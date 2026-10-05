import { useState } from "react";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { Building2, Loader2, ShieldCheck } from "lucide-react";
import { toast } from "sonner";
import { Button } from "@/components/ui/button";
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog";
import { api } from "@/lib/api/client";
import { useAuth } from "@/lib/auth";
import type {
  ContactDetail,
  ContactFieldType,
  Employee,
} from "@/lib/api/types";
import { CONTACT_FIELD_META } from "@/lib/contact-fields";
import { formatRelativeVerification } from "@/lib/format";
import { TableCell, TableRow } from "@/components/ui/table";

const CONTACT_FIELD_TYPES: ContactFieldType[] = [
  "MOBILE",
  "EMAIL",
  "DIRECT_LINE",
  "LINKEDIN",
];

export function EmployeeCard({
  employee,
  showCompany = false,
  layout = "card",
}: {
  employee: Employee;
  showCompany?: boolean;
  layout?: "card" | "table";
}) {
  const qc = useQueryClient();
  const { token } = useAuth();
  const wallet = useQuery({
    queryKey: ["wallet"],
    queryFn: api.wallet,
    enabled: !!token,
  });
  const [target, setTarget] = useState<ContactDetail | null>(null);

  const unlock = useMutation({
    mutationFn: (contactId: string) => api.unlock(contactId),
    onSuccess: async () => {
      toast.success("Contact unlocked");
      setTarget(null);
      await Promise.all([
        qc.invalidateQueries({ queryKey: ["employees", employee.companyId] }),
        qc.invalidateQueries({ queryKey: ["people"] }),
        qc.invalidateQueries({ queryKey: ["wallet"] }),
        qc.invalidateQueries({ queryKey: ["transactions"] }),
        qc.invalidateQueries({ queryKey: ["history"] }),
      ]);
    },
    onError: (err) =>
      toast.error(err instanceof Error ? err.message : "Unlock failed"),
  });

  const balance = wallet.data?.balance ?? 0;
  const insufficient = !!target && balance < target.cost;
  const contactDetails = employee.contactDetails ?? [];
  const detailsByType = new Map<ContactFieldType, ContactDetail>();
  for (const detail of contactDetails) detailsByType.set(detail.type, detail);
  const lastVerifiedAt = contactDetails.reduce<string | undefined>(
    (latest, detail) =>
      !latest || Date.parse(detail.lastVerifiedAt) > Date.parse(latest)
        ? detail.lastVerifiedAt
        : latest,
    undefined,
  );

  function renderContactDetail(detail: ContactDetail, tableLayout = false) {
    const Icon = CONTACT_FIELD_META[detail.type].icon;

    return detail.unlocked ? (
      <span
        className={`flex h-9 min-w-0 items-center gap-2 rounded-lg border border-success/40 bg-success/10 px-3 font-mono ${tableLayout ? "text-sm" : "text-xs"}`}
      >
        <Icon className="size-3.5 shrink-0" />
        <span className="truncate">{detail.value}</span>
      </span>
    ) : (
      <Button
        variant="outline"
        size="sm"
        className={`w-full justify-start gap-2 ${tableLayout ? "whitespace-normal text-sm" : ""}`}
        onClick={() => setTarget(detail)}
      >
        <Icon className="size-3.5 shrink-0" />
        {CONTACT_FIELD_META[detail.type].label} · {detail.cost} tokens
      </Button>
    );
  }

  function renderUnavailableContact(
    type: ContactFieldType,
    tableLayout = false,
  ) {
    const Icon = CONTACT_FIELD_META[type].icon;

    return (
      <Button
        variant="outline"
        size="sm"
        className={`w-full justify-start gap-2 ${tableLayout ? "whitespace-normal text-sm" : ""}`}
        disabled
        aria-label={`${CONTACT_FIELD_META[type].label} unavailable`}
      >
        <Icon className="size-3.5 shrink-0" />
        {CONTACT_FIELD_META[type].label} · unavailable
      </Button>
    );
  }

  function renderContactTypes(tableLayout = false) {
    return CONTACT_FIELD_TYPES.map((type) => {
      const detail = detailsByType.get(type);

      return (
        <div key={type} className={tableLayout ? "" : "min-w-0"}>
          {detail
            ? renderContactDetail(detail, tableLayout)
            : renderUnavailableContact(type, tableLayout)}
        </div>
      );
    });
  }

  return (
    <>
      {layout === "table" ? (
        <TableRow className="border-0 bg-background shadow-(--shadow-card) transition-shadow hover:shadow-(--shadow-elevated) [&>td]:min-w-0 [&>td]:border-y [&>td]:border-border [&>td]:px-2 [&>td]:py-3 [&>td]:sm:px-3 [&>td]:xl:py-4 [&>td:first-child]:rounded-l-lg [&>td:first-child]:border-l [&>td:last-child]:rounded-r-lg [&>td:last-child]:border-r">
          <TableCell className="min-w-0 wrap-break-word text-xs sm:text-sm">
            <p className="font-semibold">{employee.fullName}</p>
            <p className="mt-1 wrap-break-word text-xs text-muted-foreground lg:hidden">
              {employee.title} · {employee.department}
            </p>
            <p className="wrap-break-word text-xs text-muted-foreground sm:hidden">
              {employee.companyName}
              {employee.companyCountry ? ` · ${employee.companyCountry}` : ""}
            </p>
            {lastVerifiedAt && (
              <span className="mt-1 flex items-center gap-1 text-xs font-medium text-success">
                <ShieldCheck className="size-3.5 shrink-0" aria-hidden="true" />
                {formatRelativeVerification(lastVerifiedAt)}
              </span>
            )}
          </TableCell>
          <TableCell className="hidden min-w-0 wrap-break-word text-sm lg:table-cell">
            {employee.title}
            <span className="mt-1 block text-xs text-muted-foreground xl:hidden">
              {employee.department}
              {employee.companyCountry ? ` · ${employee.companyCountry}` : ""}
            </span>
          </TableCell>
          <TableCell className="hidden min-w-0 wrap-break-word text-sm xl:table-cell">
            {employee.department}
          </TableCell>
          <TableCell className="hidden min-w-0 wrap-break-word text-sm xl:table-cell">
            {employee.companyCountry || "—"}
          </TableCell>
          <TableCell className="hidden min-w-0 wrap-break-word text-xs sm:table-cell sm:text-sm">
            <span className="flex min-w-0 items-center gap-1.5">
              <Building2 className="size-3.5 shrink-0 text-muted-foreground" />
              <span className="wrap-break-word">{employee.companyName}</span>
            </span>
            <span className="mt-1 block text-xs text-muted-foreground xl:hidden">
              {employee.companyCountry || "—"}
            </span>
          </TableCell>
          <TableCell className="min-w-0">
            <div className="grid min-w-0 gap-2">{renderContactTypes(true)}</div>
          </TableCell>
        </TableRow>
      ) : (
        <li className="rounded-xl border border-border bg-background p-4">
          <div>
            <div>
              <div className="flex flex-wrap items-center gap-x-2 gap-y-1">
                <p className="font-medium">{employee.fullName}</p>
                {lastVerifiedAt && (
                  <span className="flex items-center gap-1 text-xs font-medium text-success">
                    <ShieldCheck
                      className="size-3.5 shrink-0"
                      aria-hidden="true"
                    />
                    {formatRelativeVerification(lastVerifiedAt)}
                  </span>
                )}
              </div>
              <p className="text-sm text-muted-foreground">
                {employee.title} · {employee.department}
              </p>
              {showCompany && (
                <p className="mt-1 flex items-center gap-1.5 text-xs text-muted-foreground">
                  <Building2 className="size-3" />
                  {employee.companyName}
                  {employee.companyCountry
                    ? ` · ${employee.companyCountry}`
                    : ""}
                  {employee.companyIndustry
                    ? ` · ${employee.companyIndustry}`
                    : ""}
                </p>
              )}
            </div>
            <div className="mt-4 grid gap-2 border-t border-border pt-4 sm:grid-cols-2 lg:grid-cols-4">
              {renderContactTypes()}
            </div>
          </div>
        </li>
      )}

      <Dialog open={!!target} onOpenChange={(o) => !o && setTarget(null)}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>Unlock this contact?</DialogTitle>
            <DialogDescription>
              {target && (
                <>
                  Revealing {employee.fullName}'s{" "}
                  {CONTACT_FIELD_META[target.type].noun} costs{" "}
                  <strong>{target.cost} tokens</strong>. Your balance is{" "}
                  {balance} tokens.
                </>
              )}
            </DialogDescription>
          </DialogHeader>
          {insufficient && (
            <p className="rounded-lg border border-destructive/30 bg-destructive/5 px-3 py-2 text-sm text-destructive">
              Not enough tokens. Buy a token package to continue.
            </p>
          )}
          <DialogFooter>
            <Button variant="ghost" onClick={() => setTarget(null)}>
              Cancel
            </Button>
            <Button
              disabled={unlock.isPending || insufficient}
              onClick={() => target && unlock.mutate(target.id)}
            >
              {unlock.isPending && <Loader2 className="size-4 animate-spin" />}
              Confirm unlock
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>
    </>
  );
}
