import { createFileRoute, Link } from "@tanstack/react-router";
import { useQuery } from "@tanstack/react-query";
import {
  ArrowLeft,
  Building2,
  CalendarDays,
  Factory,
  Globe,
  MapPin,
  MapPinned,
  ShieldCheck,
  Users,
} from "lucide-react";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { EmptyState, ErrorState, LoadingState } from "@/components/states";
import { EmployeeCard } from "@/components/employee-card";
import { api } from "@/lib/api/client";
import { titleCase } from "@/lib/format";

export const Route = createFileRoute("/_authenticated/companies/$companyId")({
  head: () => ({
    meta: [
      { title: "Company profile — Datasphere" },
      { name: "description", content: "Company details and unlockable employee contacts." },
      { property: "og:title", content: "Company profile — Datasphere" },
      { property: "og:description", content: "Company details and unlockable employee contacts." },
    ],
  }),
  component: CompanyProfilePage,
});

function CompanyProfilePage() {
  const { companyId } = Route.useParams();

  const company = useQuery({
    queryKey: ["company", companyId],
    queryFn: () => api.company(companyId),
  });
  const employees = useQuery({
    queryKey: ["employees", companyId],
    queryFn: () => api.employeesOfCompany(companyId),
  });

  if (company.isLoading) return <LoadingState label="Loading company…" />;
  if (company.isError || !company.data) {
    return <ErrorState error={company.error} onRetry={() => void company.refetch()} />;
  }

  const c = company.data;

  return (
    <div className="space-y-6">
      <Button variant="ghost" size="sm" className="-ml-2 gap-2" asChild>
        <Link to="/search">
          <ArrowLeft className="size-4" /> Back to Company Hub
        </Link>
      </Button>

      <Card className="shadow-[var(--shadow-card)]">
        <CardHeader>
          <div className="flex flex-wrap items-start justify-between gap-4">
            <div className="flex items-start gap-4">
              <span className="grid size-14 shrink-0 place-items-center rounded-2xl bg-accent text-accent-foreground">
                <Building2 className="size-7" />
              </span>
              <div>
                <CardTitle className="flex items-center gap-2 text-2xl">
                  {c.name}
                  {c.verified && (
                    <Badge variant="secondary" className="gap-1">
                      <ShieldCheck className="size-3" /> Verified
                    </Badge>
                  )}
                </CardTitle>
                <p className="mt-1 flex flex-wrap items-center gap-x-3 gap-y-1 text-sm text-muted-foreground">
                  <span className="flex items-center gap-1">
                    <MapPin className="size-3.5" /> {c.city}, {c.country}
                  </span>
                  <a href={c.website}>
                  <span className="flex items-center gap-1">
                    
                    <Globe className="size-3.5" /> {c.website}
                    
                  </span>
                  </a>
                  <span className="flex items-center gap-1">
                    <Users className="size-3.5" /> {c.employeeCount.toLocaleString("en-US")} employees
                  </span>
                </p>
              </div>
            </div>
            <Badge variant="outline">{titleCase(c.type)}</Badge>
          </div>
        </CardHeader>
        <CardContent className="space-y-6">
          <p className="max-w-3xl text-sm leading-relaxed text-muted-foreground">{c.description}</p>

          <div className="grid gap-4 sm:grid-cols-3">
            <div className="rounded-xl border border-border bg-surface p-4">
              <p className="flex items-center gap-1.5 text-xs font-medium uppercase tracking-wide text-muted-foreground">
                <CalendarDays className="size-3.5" /> Established
              </p>
              <p className="mt-1 text-lg font-semibold">{c.foundedYear}</p>
            </div>
            <div className="rounded-xl border border-border bg-surface p-4">
              <p className="flex items-center gap-1.5 text-xs font-medium uppercase tracking-wide text-muted-foreground">
                <Factory className="size-3.5" /> Industry
              </p>
              <p className="mt-1 text-lg font-semibold">{c.industry}</p>
            </div>
            <div className="rounded-xl border border-border bg-surface p-4">
              <p className="flex items-center gap-1.5 text-xs font-medium uppercase tracking-wide text-muted-foreground">
                <MapPinned className="size-3.5" />
                Geographic presence
              </p>
              <div className="mt-2 flex flex-wrap gap-1.5">
                {c.geographicPresence.map((country) => (
                  <Badge key={country} variant="secondary">
                    {country}
                  </Badge>
                ))}
              </div>
            </div>
          </div>
        </CardContent>
      </Card>

      <div>
        <h2 className="text-lg font-semibold">People at {c.name}</h2>
        <p className="mt-0.5 text-sm text-muted-foreground">
          Unlock mobile, direct line, email or LinkedIn for any contact below.
        </p>
      </div>

      {employees.isLoading ? (
        <LoadingState label="Loading people…" />
      ) : employees.isError ? (
        <ErrorState error={employees.error} onRetry={() => void employees.refetch()} />
      ) : !employees.data?.length ? (
        <EmptyState title="No contacts listed for this company yet" />
      ) : (
        <ul className="space-y-3">
          {employees.data.map((e) => (
            <EmployeeCard
              key={e.id}
              employee={{
                ...e,
                companyId: c.id,
                companyName: c.name,
                companyIndustry: c.industry,
                companyCountry: c.country,
              }}
            />
          ))}
        </ul>
      )}
    </div>
  );
}
