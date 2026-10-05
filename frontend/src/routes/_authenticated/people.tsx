import { createFileRoute, Link, useNavigate } from "@tanstack/react-router";
import { useEffect, useState } from "react";
import { useQuery } from "@tanstack/react-query";
import { Search as SearchIcon, Sparkles, Users } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Card, CardContent } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { EmptyState, ErrorState, LoadingState } from "@/components/states";
import { EmployeeCard } from "@/components/employee-card";
import { FilterSelect } from "@/components/filter-select";
import { api } from "@/lib/api/client";
import {
  Table,
  TableBody,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table";

interface PeopleParams {
  keyword?: string | undefined;
  title?: string | undefined;
  department?: string | undefined;
  industry?: string | undefined;
  country?: string | undefined;
  page?: number | undefined;
  run?: boolean | undefined;
}

const str = (v: unknown) => (typeof v === "string" ? v : undefined);

export const Route = createFileRoute("/_authenticated/people")({
  validateSearch: (search: Record<string, unknown>): PeopleParams => ({
    keyword: str(search["keyword"]),
    title: str(search["title"]),
    department: str(search["department"]),
    industry: str(search["industry"]),
    country: str(search["country"]),
    page: typeof search["page"] === "number" ? search["page"] : undefined,
    run: search["run"] === true,
  }),
  head: () => ({
    meta: [
      { title: "People Hub — Datasphere" },
      {
        name: "description",
        content:
          "Search decision-makers by name, role, department, industry or country, then unlock their contacts.",
      },
      { property: "og:title", content: "People Hub — Datasphere" },
      {
        property: "og:description",
        content: "Find decision-makers directly and unlock verified contacts.",
      },
    ],
  }),
  component: PeoplePage,
});

function PeoplePage() {
  const params = Route.useSearch();
  const navigate = useNavigate({ from: "/people" });
  const [keyword, setKeyword] = useState(params.keyword ?? "");
  const [title, setTitle] = useState(params.title);
  const [department, setDepartment] = useState(params.department);
  const [industry, setIndustry] = useState(params.industry);
  const [country, setCountry] = useState(params.country);
  const [searchVersion, setSearchVersion] = useState(0);
  const filters = useQuery({ queryKey: ["people-filters"], queryFn: api.peopleFilters });

  useEffect(() => {
    setKeyword(params.keyword ?? "");
    setTitle(params.title);
    setDepartment(params.department);
    setIndustry(params.industry);
    setCountry(params.country);
  }, [params.keyword, params.title, params.department, params.industry, params.country]);

  const page = params.page ?? 0;
  const hasCommittedFilters = Boolean(
    params.keyword || params.title || params.department || params.industry || params.country,
  );
  const shouldRunSearch = params.run === true || !hasCommittedFilters;
  const query = useQuery({
    queryKey: ["people", params, searchVersion],
    enabled: shouldRunSearch,
    refetchOnWindowFocus: false,
    refetchOnReconnect: false,
    queryFn: () =>
      api.searchPeople({
        ...(params.keyword ? { keyword: params.keyword } : {}),
        ...(params.title ? { title: params.title } : {}),
        ...(params.department ? { department: params.department } : {}),
        ...(params.industry ? { industry: params.industry } : {}),
        ...(params.country ? { country: params.country } : {}),
        page,
      }),
  });

  function submitSearch() {
    setSearchVersion((version) => version + 1);
    void navigate({
      search: {
        keyword: keyword.trim() || undefined,
        title,
        department,
        industry,
        country,
        page: 0,
        run: true,
      },
    });
  }

  return (
    <div className="space-y-6">
      <div>
        <div className="flex flex-wrap items-center gap-3">
          <h1 className="flex items-center gap-2 text-2xl font-semibold">
            <Users className="size-6 text-primary" /> People Hub
          </h1>
        </div>
      </div>

      <Card className="shadow-(--shadow-card)">
        <CardContent className="pt-6">
          <form
            className="grid gap-4 md:grid-cols-4"
            onSubmit={(e) => {
              e.preventDefault();
              submitSearch();
            }}
          >
            <div className="space-y-2 md:col-span-4 lg:col-span-1">
              <Label htmlFor="people-keyword">Name or role</Label>
              <div className="relative">
                <SearchIcon className="absolute top-1/2 left-3 size-4 -translate-y-1/2 text-muted-foreground" />
                <Input
                  id="people-keyword"
                  className="pl-9"
                  placeholder="e.g. Amira, Head of Sales…"
                  value={keyword}
                  onChange={(e) => setKeyword(e.target.value)}
                />
              </div>
            </div>

            <FilterSelect
              label="Job title"
              value={title}
              options={filters.data?.titles ?? []}
              onChange={setTitle}
            />
            <FilterSelect
              label="Department"
              value={department}
              options={filters.data?.departments ?? []}
              onChange={setDepartment}
            />
            <FilterSelect
              label="Industry"
              value={industry}
              options={filters.data?.industries ?? []}
              onChange={setIndustry}
            />
            <FilterSelect
              label="Country"
              value={country}
              options={filters.data?.countries ?? []}
              onChange={setCountry}
            />

            <div className="flex gap-2 md:col-span-4">
              <Button type="submit">Search people</Button>
              <Button
                type="button"
                variant="ghost"
                onClick={() => {
                  setKeyword("");
                  setTitle(undefined);
                  setDepartment(undefined);
                  setIndustry(undefined);
                  setCountry(undefined);
                  void navigate({ search: {} });
                }}
              >
                Clear filters
              </Button>
            </div>
          </form>
        </CardContent>
      </Card>

      {!shouldRunSearch ? (
        <EmptyState title="Ready to search" description="Submit your selected filters to see matching people." />
      ) : query.isLoading ? (
        <LoadingState label="Searching people…" />
      ) : query.isError ? (
        <ErrorState error={query.error} onRetry={() => void query.refetch()} />
      ) : !query.data?.content.length ? (
        <EmptyState
          title="No people match these filters"
          description="Try a broader keyword or clear one of the filters."
        />
      ) : (
        <>
          <p className="text-sm text-muted-foreground">
            {query.data.totalElements} people found
          </p>
          <Table className="w-full table-fixed border-separate border-spacing-x-0 border-spacing-y-3 text-xs sm:text-sm">
            <TableHeader>
              <TableRow>
                <TableHead className="w-[42%] px-2 text-xs sm:w-[32%] sm:px-3 lg:w-[28%] xl:w-[19%]">
                  Name
                </TableHead>
                <TableHead className="hidden px-2 text-xs lg:table-cell lg:w-[17%] xl:w-[15%]">
                  Job title
                </TableHead>
                <TableHead className="hidden px-2 text-xs xl:table-cell xl:w-[14%]">
                  Department
                </TableHead>
                <TableHead className="hidden px-2 text-xs xl:table-cell xl:w-[10%]">
                  Country
                </TableHead>
                <TableHead className="hidden px-2 text-xs sm:table-cell sm:w-[20%] lg:w-[22%] xl:w-[15%]">
                  Company
                </TableHead>
                <TableHead className="w-[58%] px-2 text-xs sm:w-[48%] sm:px-3 lg:w-[33%] xl:w-[27%]">
                  Contact details
                </TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {query.data.content.map((employee) => (
                <EmployeeCard
                  key={employee.id}
                  employee={employee}
                  showCompany
                  layout="table"
                />
              ))}
            </TableBody>
          </Table>
          <div className="flex items-center justify-between pt-2">
            <Button
              variant="outline"
              size="sm"
              disabled={page === 0}
              onClick={() =>
                void navigate({
                  search: (prev) => ({ ...prev, page: page - 1 }),
                })
              }
            >
              Previous
            </Button>
            <span className="text-sm text-muted-foreground">
              Page {page + 1} of {query.data.totalPages}
            </span>
            <Button
              variant="outline"
              size="sm"
              disabled={page + 1 >= query.data.totalPages}
              onClick={() =>
                void navigate({
                  search: (prev) => ({ ...prev, page: page + 1 }),
                })
              }
            >
              Next
            </Button>
          </div>
        </>
      )}
    </div>
  );
}
