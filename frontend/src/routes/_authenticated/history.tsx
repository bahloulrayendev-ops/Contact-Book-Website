import { createFileRoute } from "@tanstack/react-router";
import { useEffect, useState, type FormEvent } from "react";
import { useQuery } from "@tanstack/react-query";
import { Check, Download,History, Folder, FolderMinus, FolderPlus } from "lucide-react";
import { toast } from "sonner";
import { CONTACT_FIELD_META } from "@/lib/contact-fields";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Card, CardContent } from "@/components/ui/card";
import { Checkbox } from "@/components/ui/checkbox";
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table";
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs";
import { EmptyState, ErrorState, LoadingState } from "@/components/states";
import { api } from "@/lib/api/client";
import type { UnlockRecord } from "@/lib/api/types";
import { formatDateTime } from "@/lib/format";
import { useAuth } from "@/lib/auth";

interface UnlockPackage {
  id: string;
  name: string;
  recordIds: string[];
}

const packageStoragePrefix = "datasphere.unlock-packages.v1";

function readPackages(storageKey: string): UnlockPackage[] {
  try {
    const parsed: unknown = JSON.parse(
      window.localStorage.getItem(storageKey) ?? "[]",
    );
    if (!Array.isArray(parsed)) return [];
    return parsed.filter(
      (item): item is UnlockPackage =>
        typeof item === "object" &&
        item !== null &&
        typeof item.id === "string" &&
        typeof item.name === "string" &&
        Array.isArray(item.recordIds) &&
        item.recordIds.every((id: unknown) => typeof id === "string"),
    );
  } catch {
    return [];
  }
}

function csvCell(value: string | number) {
  return `"${String(value).replaceAll('"', '""')}"`;
}

function downloadCsv(records: UnlockRecord[]) {
  const rows: (string | number)[][] = [
    [
      "Name",
      "Company",
      "Contact type",
      "Contact value",
      "Tokens spent",
      "Unlocked at",
    ],
    ...records.map((record) => [
      record.employeeName,
      record.companyName,
      CONTACT_FIELD_META[record.fieldType].label,
      record.value,
      record.tokensSpent,
      formatDateTime(record.unlockedAt),
    ]),
  ];
  const csv = rows.map((row) => row.map(csvCell).join(",")).join("\r\n");
  const url = URL.createObjectURL(
    new Blob(["\uFEFF", csv], { type: "text/csv;charset=utf-8" }),
  );
  const link = document.createElement("a");
  link.href = url;
  link.download = `unlocked-contacts-${new Date().toISOString().slice(0, 10)}.csv`;
  link.click();
  URL.revokeObjectURL(url);
}

export const Route = createFileRoute("/_authenticated/history")({
  head: () => ({
    meta: [
      { title: "History — Datasphere" },
      {
        name: "description",
        content: "Every contact you unlocked and every search you ran.",
      },
      { property: "og:title", content: "History — Datasphere" },
      {
        property: "og:description",
        content: "Your unlock and search history.",
      },
    ],
  }),
  component: HistoryPage,
});

function HistoryPage() {
  const { user } = useAuth();
  const storageKey = user ? `${packageStoragePrefix}:${user.id}` : null;
  const [packages, setPackages] = useState<UnlockPackage[]>([]);
  const [loadedUserId, setLoadedUserId] = useState<string | null>(null);
  const [activePackageId, setActivePackageId] = useState("all");
  const [destinationPackageId, setDestinationPackageId] = useState("");
  const [selectedIds, setSelectedIds] = useState<Set<string>>(() => new Set());
  const [packageDialogOpen, setPackageDialogOpen] = useState(false);
  const [creatingPackage, setCreatingPackage] = useState(false);
  const [packageName, setPackageName] = useState("");
  const unlocks = useQuery({
    queryKey: ["history", "unlocks", "all"],
    queryFn: async () => {
      const firstPage = await api.unlockHistory(0);
      const records = [...firstPage.content];
      for (let page = 1; page < firstPage.totalPages; page += 1) {
        const nextPage = await api.unlockHistory(page);
        records.push(...nextPage.content);
      }
      return { ...firstPage, content: records };
    },
  });
  const searches = useQuery({
    queryKey: ["history", "searches", 0],
    queryFn: () => api.searchHistory(0),
  });
  useEffect(() => {
    if (!storageKey || !user) return;
    setPackages(readPackages(storageKey));
    setLoadedUserId(user.id);
  }, [storageKey, user]);

  useEffect(() => {
    if (!storageKey || !user || loadedUserId !== user.id) return;
    window.localStorage.setItem(storageKey, JSON.stringify(packages));
  }, [loadedUserId, packages, storageKey, user]);

  const records = unlocks.data?.content ?? [];
  const activePackage = packages.find((item) => item.id === activePackageId);
  const visibleRecords = activePackage
    ? records.filter((record) => activePackage.recordIds.includes(record.id))
    : records;
  const selectedRecords = records.filter((record) =>
    selectedIds.has(record.id),
  );
  const allVisibleSelected =
    visibleRecords.length > 0 &&
    visibleRecords.every((record) => selectedIds.has(record.id));

  function toggleRecord(recordId: string, selected: boolean) {
    setSelectedIds((current) => {
      const next = new Set(current);
      if (selected) next.add(recordId);
      else next.delete(recordId);
      return next;
    });
  }

  function toggleVisibleRecords(selected: boolean) {
    setSelectedIds((current) => {
      const next = new Set(current);
      for (const record of visibleRecords) {
        if (selected) next.add(record.id);
        else next.delete(record.id);
      }
      return next;
    });
  }

  function createPackage(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    const name = packageName.trim();
    if (!name) return;
    const created = {
      id: crypto.randomUUID(),
      name,
      recordIds: [...selectedIds],
    };
    setPackages((current) => [...current, created]);
    setSelectedIds(new Set());
    setPackageName("");
    setPackageDialogOpen(false);
    setCreatingPackage(false);
    toast.success(`Package "${name}" created with selected contacts`);
  }

  function addSelectedToPackage() {
    if (!destinationPackageId || selectedIds.size === 0) return;
    setPackages((current) =>
      current.map((item) =>
        item.id === destinationPackageId
          ? {
              ...item,
              recordIds: [...new Set([...item.recordIds, ...selectedIds])],
            }
          : item,
      ),
    );
    toast.success(
      `${selectedIds.size} contact${selectedIds.size === 1 ? "" : "s"} added to package`,
    );
    setSelectedIds(new Set());
    setPackageDialogOpen(false);
    setDestinationPackageId("");
  }

  function openPackageDialog() {
    setDestinationPackageId("");
    setPackageName("");
    setCreatingPackage(false);
    setPackageDialogOpen(true);
  }

  function removeSelectedFromPackage() {
    if (!activePackage || selectedIds.size === 0) return;
    setPackages((current) =>
      current.map((item) =>
        item.id === activePackage.id
          ? {
              ...item,
              recordIds: item.recordIds.filter((id) => !selectedIds.has(id)),
            }
          : item,
      ),
    );
    setSelectedIds(new Set());
    toast.success("Selected contacts removed from package");
  }

  return (
    <div className="space-y-6">
      <div>
        <h1 className="flex items-center gap-2 text-2xl font-semibold"><History className="size-5 text-primary" /> History</h1>
        <p className="text-sm text-muted-foreground">
          Everything you've unlocked and searched for.
        </p>
      </div>

      <Tabs defaultValue="unlocks">
        <TabsList>
          <TabsTrigger value="unlocks">Unlocked contacts</TabsTrigger>
          <TabsTrigger value="searches">Past searches</TabsTrigger>
        </TabsList>

        <TabsContent value="unlocks">
          <Card className="shadow-(--shadow-card)">
            <CardContent className="p-0">
              <div className="flex flex-wrap items-center justify-between gap-3 border-b p-4">
                <div className="flex flex-wrap items-center gap-2">
                  <Select
                    value={activePackageId}
                    onValueChange={(value) => {
                      setActivePackageId(value);
                      setSelectedIds(new Set());
                    }}
                  >
                    <SelectTrigger
                      className="w-52"
                      aria-label="Filter unlocked contacts by package"
                    >
                      <SelectValue />
                    </SelectTrigger>
                    <SelectContent>
                      <SelectItem value="all">
                        All unlocked ({records.length})
                      </SelectItem>
                      {packages.map((item) => (
                        <SelectItem key={item.id} value={item.id}>
                          {item.name} ({item.recordIds.length})
                        </SelectItem>
                      ))}
                    </SelectContent>
                  </Select>
                </div>
                <div className="flex flex-wrap items-center gap-2">
                  {selectedIds.size > 0 && (
                    <>
                      <span className="text-sm text-muted-foreground">
                        {selectedIds.size} selected
                      </span>
                      <Button variant="secondary" onClick={openPackageDialog}>
                        <FolderPlus className="size-4" />
                        Add to package
                      </Button>
                      {activePackage && (
                        <Button
                          variant="ghost"
                          onClick={removeSelectedFromPackage}
                        >
                          <FolderMinus className="size-4" />
                          Remove
                        </Button>
                      )}
                    </>
                  )}
                  <Button
                    variant="outline"
                    disabled={!selectedRecords.length}
                    onClick={() => downloadCsv(selectedRecords)}
                  >
                    <Download className="size-4" />
                    Export selected CSV
                  </Button>
                </div>
              </div>
              {unlocks.isLoading ? (
                <LoadingState />
              ) : unlocks.isError ? (
                <ErrorState
                  error={unlocks.error}
                  onRetry={() => void unlocks.refetch()}
                />
              ) : !visibleRecords.length ? (
                <div className="p-6">
                  <EmptyState
                    title={
                      activePackage
                        ? "This package is empty"
                        : "No unlocked contacts"
                    }
                    description={
                      activePackage
                        ? "Select contacts from All unlocked and add them to this package."
                        : "Unlocks you pay for will be listed here."
                    }
                  />
                </div>
              ) : (
                <div className="overflow-x-auto">
                  <Table>
                    <TableHeader>
                      <TableRow>
                        <TableHead>
                          <span className="flex items-center gap-2">
                            <Checkbox
                              aria-label="Select all visible contacts"
                              checked={allVisibleSelected}
                              onCheckedChange={(checked) =>
                                toggleVisibleRecords(checked === true)
                              }
                            />
                            Name
                          </span>
                        </TableHead>
                        <TableHead>Company</TableHead>
                        <TableHead>Field</TableHead>
                        <TableHead>Value</TableHead>
                        <TableHead className="text-right">Tokens</TableHead>
                        <TableHead>Date</TableHead>
                      </TableRow>
                    </TableHeader>
                    <TableBody>
                      {visibleRecords.map((u) => (
                        <TableRow key={u.id}>
                          <TableCell>
                            <span className="flex items-center gap-2">
                              <Checkbox
                                aria-label={`Select ${u.employeeName}`}
                                checked={selectedIds.has(u.id)}
                                onCheckedChange={(checked) =>
                                  toggleRecord(u.id, checked === true)
                                }
                              />
                              <span className="font-medium">
                                {u.employeeName}
                              </span>
                            </span>
                          </TableCell>
                          <TableCell className="text-muted-foreground">
                            {u.companyName}
                          </TableCell>
                          <TableCell>
                            <Badge variant="secondary" className="gap-1">
                              {(() => {
                                const Icon =
                                  CONTACT_FIELD_META[u.fieldType].icon;
                                return <Icon className="size-3" />;
                              })()}
                              {CONTACT_FIELD_META[u.fieldType].label}
                            </Badge>
                          </TableCell>
                          <TableCell className="font-mono text-xs">
                            {u.value}
                          </TableCell>
                          <TableCell className="text-right">
                            {u.tokensSpent}
                          </TableCell>
                          <TableCell className="text-muted-foreground">
                            {formatDateTime(u.unlockedAt)}
                          </TableCell>
                        </TableRow>
                      ))}
                    </TableBody>
                  </Table>
                </div>
              )}
            </CardContent>
          </Card>
          <Dialog
            open={packageDialogOpen}
            onOpenChange={(open) => {
              setPackageDialogOpen(open);
              if (!open) setCreatingPackage(false);
            }}
          >
            <DialogContent>
              {creatingPackage ? (
                <form onSubmit={createPackage} className="space-y-4">
                  <DialogHeader>
                    <DialogTitle>Create package</DialogTitle>
                    <DialogDescription>
                      The {selectedIds.size} selected contact
                      {selectedIds.size === 1 ? "" : "s"} will be added.
                    </DialogDescription>
                  </DialogHeader>
                  <div className="space-y-2">
                    <Label htmlFor="unlock-package-name">Package name</Label>
                    <Input
                      id="unlock-package-name"
                      value={packageName}
                      onChange={(event) => setPackageName(event.target.value)}
                      maxLength={60}
                      autoFocus
                      required
                    />
                  </div>
                  <DialogFooter>
                    <Button
                      type="button"
                      variant="ghost"
                      onClick={() => setCreatingPackage(false)}
                    >
                      Back to packages
                    </Button>
                    <Button type="submit">Create package</Button>
                  </DialogFooter>
                </form>
              ) : (
                <div className="space-y-4">
                  <DialogHeader>
                    <DialogTitle>Add contacts to a package</DialogTitle>
                    <DialogDescription>
                      Choose an existing package or create a new one for the{" "}
                      {selectedIds.size} selected contact
                      {selectedIds.size === 1 ? "" : "s"}.
                    </DialogDescription>
                  </DialogHeader>
                  <div className="max-h-64 space-y-2 overflow-y-auto">
                    {packages.map((item) => (
                      <Button
                        key={item.id}
                        type="button"
                        variant="outline"
                        aria-pressed={destinationPackageId === item.id}
                        className={`h-auto w-full justify-between px-3 py-3 ${destinationPackageId === item.id ? "border-primary bg-accent" : ""}`}
                        onClick={() => setDestinationPackageId(item.id)}
                      >
                        <span className="flex min-w-0 items-center gap-2">
                          <Folder className="size-4 shrink-0 text-muted-foreground" />
                          <span className="truncate">{item.name}</span>
                        </span>
                        <span className="flex items-center gap-2">
                          <Badge variant="secondary">
                            {item.recordIds.length}
                          </Badge>
                          {destinationPackageId === item.id && (
                            <Check className="size-4 text-primary" />
                          )}
                        </span>
                      </Button>
                    ))}
                    {!packages.length && (
                      <p className="rounded-md border border-dashed p-4 text-center text-sm text-muted-foreground">
                        No packages yet. Create one to organize these contacts.
                      </p>
                    )}
                  </div>
                  <Button
                    type="button"
                    variant="outline"
                    className="w-full justify-start"
                    onClick={() => setCreatingPackage(true)}
                  >
                    <FolderPlus className="size-4" />
                    Create new package
                  </Button>
                  <DialogFooter>
                    <Button
                      type="button"
                      variant="ghost"
                      onClick={() => setPackageDialogOpen(false)}
                    >
                      Cancel
                    </Button>
                    <Button
                      type="button"
                      disabled={!destinationPackageId}
                      onClick={addSelectedToPackage}
                    >
                      Add to package
                    </Button>
                  </DialogFooter>
                </div>
              )}
            </DialogContent>
          </Dialog>
        </TabsContent>

        <TabsContent value="searches">
          <Card className="shadow-(--shadow-card)">
            <CardContent className="p-0">
              {searches.isLoading ? (
                <LoadingState />
              ) : searches.isError ? (
                <ErrorState error={searches.error} onRetry={() => void searches.refetch()} />
              ) : !searches.data?.content.length ? (
                <div className="p-6">
                  <EmptyState title="No searches yet" description="Your successful company and people searches will appear here." />
                </div>
              ) : (
                <div className="overflow-x-auto">
                  <Table>
                    <TableHeader>
                      <TableRow>
                        <TableHead>Hub</TableHead>
                        <TableHead>Query</TableHead>
                        <TableHead>Filters</TableHead>
                        <TableHead className="text-right">Results</TableHead>
                        <TableHead>Date</TableHead>
                      </TableRow>
                    </TableHeader>
                    <TableBody>
                      {searches.data.content.map((search) => (
                        <TableRow key={search.id}>
                          <TableCell>
                            <Badge variant="secondary">{search.type === "company" ? "Company" : "People"}</Badge>
                          </TableCell>
                          <TableCell className="font-medium">{search.query}</TableCell>
                          <TableCell className="text-muted-foreground">{search.filters}</TableCell>
                          <TableCell className="text-right">{search.resultCount}</TableCell>
                          <TableCell className="text-muted-foreground">{formatDateTime(search.searchedAt)}</TableCell>
                        </TableRow>
                      ))}
                    </TableBody>
                  </Table>
                </div>
              )}
            </CardContent>
          </Card>
        </TabsContent>
      </Tabs>
    </div>
  );
}
