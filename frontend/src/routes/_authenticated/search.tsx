import { createFileRoute, Link, useNavigate } from "@tanstack/react-router";
import { useEffect, useState } from "react";
import { useQuery } from "@tanstack/react-query";
import { Building2, ChevronRight, Globe, Search as SearchIcon, ShieldCheck, Sparkles } from "lucide-react";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Card, CardContent } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { EmptyState, ErrorState, LoadingState } from "@/components/states";
import { FilterSelect } from "@/components/filter-select";
import { api } from "@/lib/api/client";
import { titleCase } from "@/lib/format";
import type { Company } from "@/lib/api/types";

interface SearchParams {
  keyword?: string | undefined;
  industry?: string | undefined;
  country?: string | undefined;
  companyType?: string | undefined;
  page?: number | undefined;
  run?: boolean | undefined;
}

export const Route = createFileRoute("/_authenticated/search")({
  validateSearch: (search: Record<string, unknown>): SearchParams => ({
    keyword: typeof search["keyword"] === "string" ? search["keyword"] : undefined,
    industry: typeof search["industry"] === "string" ? search["industry"] : undefined,
    country: typeof search["country"] === "string" ? search["country"] : undefined,
    companyType: typeof search["companyType"] === "string" ? search["companyType"] : undefined,
    page: typeof search["page"] === "number" ? search["page"] : undefined,
    run: search["run"] === true,
  }),
  head: () => ({
    meta: [
      { title: "Company Hub — Datasphere" },
      {
        name: "description",
        content:
          "Filter importers, exporters, manufacturers and distributors, open a company and unlock its people's contacts.",
      },
      { property: "og:title", content: "Company Hub — Datasphere" },
      { property: "og:description", content: "Search verified B2B company data and the people behind it." },
    ],
  }),
  component: SearchPage,
});

function SearchPage() {
  const params = Route.useSearch();
  const navigate = useNavigate({ from: "/search" });
  const [keyword, setKeyword] = useState(params.keyword ?? "");
  const [industry, setIndustry] = useState(params.industry);
  const [country, setCountry] = useState(params.country);
  const [companyType, setCompanyType] = useState(params.companyType);
  const [searchVersion, setSearchVersion] = useState(0);
  const filters = useQuery({ queryKey: ["company-filters"], queryFn: api.companyFilters });

  useEffect(() => {
    setKeyword(params.keyword ?? "");
    setIndustry(params.industry);
    setCountry(params.country);
    setCompanyType(params.companyType);
  }, [params.keyword, params.industry, params.country, params.companyType]);

  const page = params.page ?? 0;
  const hasCommittedFilters = Boolean(params.keyword || params.industry || params.country || params.companyType);
  const shouldRunSearch = params.run === true || !hasCommittedFilters;
  const query = useQuery({
    queryKey: ["companies", params, searchVersion],
    enabled: shouldRunSearch,
    refetchOnWindowFocus: false,
    refetchOnReconnect: false,
    queryFn: () =>
      api.searchCompanies({
        ...(params.keyword ? { keyword: params.keyword } : {}),
        ...(params.industry ? { industry: params.industry } : {}),
        ...(params.country ? { country: params.country } : {}),
        ...(params.companyType ? { companyType: params.companyType } : {}),
        page,
      }),
  });

  function submitSearch() {
    setSearchVersion((version) => version + 1);
    void navigate({
      search: {
        keyword: keyword.trim() || undefined,
        industry,
        country,
        companyType,
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
            <Building2 className="size-6 text-primary" /> Company Hub
          </h1>
        </div>
      </div>

      <Card className="shadow-[var(--shadow-card)]">
        <CardContent className="pt-6">
          <form
            className="grid gap-4 md:grid-cols-4"
            onSubmit={(e) => {
              e.preventDefault();
              submitSearch();
            }}
          >
            <div className="space-y-2 md:col-span-4 lg:col-span-1">
              <Label htmlFor="keyword">Keyword</Label>
              <div className="relative">
                <SearchIcon className="absolute top-1/2 left-3 size-4 -translate-y-1/2 text-muted-foreground" />
                <Input
                  id="keyword"
                  className="pl-9"
                  placeholder="Company, industry…"
                  value={keyword}
                  onChange={(e) => setKeyword(e.target.value)}
                />
              </div>
            </div>

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
            <FilterSelect
              label="Company type"
              value={companyType}
              options={filters.data?.types ?? []}
              format={titleCase}
              onChange={setCompanyType}
            />

            <div className="flex gap-2 md:col-span-4">
              <Button type="submit">Search companies</Button>
              <Button
                type="button"
                variant="ghost"
                onClick={() => {
                  setKeyword("");
                  setIndustry(undefined);
                  setCountry(undefined);
                  setCompanyType(undefined);
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
        <EmptyState title="Ready to search" description="Submit your selected filters to see matching companies." />
      ) : query.isLoading ? (
        <LoadingState label="Searching companies…" />
      ) : query.isError ? (
        <ErrorState error={query.error} onRetry={() => void query.refetch()} />
      ) : !query.data?.content.length ? (
        <EmptyState
          title="No companies match these filters"
          description="Try a broader keyword or clear one of the filters."
        />
      ) : (
        <>
          <p className="text-sm text-muted-foreground">{query.data.totalElements} companies found</p>
          <div className="space-y-3">
            {query.data.content.map((company) => (
              <CompanyRow key={company.id} company={company} />
            ))}
          </div>
          <div className="flex items-center justify-between pt-2">
            <Button
              variant="outline"
              size="sm"
              disabled={page === 0}
              onClick={() => void navigate({ search: (prev) => ({ ...prev, page: page - 1 }) })}
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
              onClick={() => void navigate({ search: (prev) => ({ ...prev, page: page + 1 }) })}
            >
              Next
            </Button>
          </div>
        </>
      )}
    </div>
  );
}

function CompanyRow({ company }: { company: Company }) {
  return (
    <Card className="overflow-hidden shadow-[var(--shadow-card)] transition-shadow hover:shadow-[var(--shadow-elevated)]">
      <Link
        to="/companies/$companyId"
        params={{ companyId: company.id }}
        className="flex w-full items-center gap-4 px-5 py-4 text-left transition-colors hover:bg-accent/50"
      >
        <span className="grid size-10 shrink-0 place-items-center rounded-xl bg-accent text-accent-foreground">
          <Building2 className="size-5" />
        </span>
        <span className="min-w-0 flex-1">
          <span className="flex items-center gap-2">
            <span className="truncate font-medium">{company.name}</span>
            {company.verified && (
              <Badge variant="secondary" className="gap-1">
                <ShieldCheck className="size-3" /> Verified
              </Badge>
            )}
          </span>
          <span className="mt-0.5 flex flex-wrap gap-x-3 text-xs text-muted-foreground">
            <span>{company.industry}</span>
            <span>
              {company.city}, {company.country}
            </span>
            <span className="flex items-center gap-1">
              <Globe className="size-3" /> {company.website}
            </span>
          </span>
        </span>
        <Badge variant="outline" className="hidden sm:inline-flex">
          {titleCase(company.type)}
        </Badge>
        <ChevronRight className="size-4 shrink-0 text-muted-foreground" />
      </Link>
    </Card>
  );
}
