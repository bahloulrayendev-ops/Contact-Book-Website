import { createFileRoute, Link } from "@tanstack/react-router";
import { useEffect, useState } from "react";
import { useQuery } from "@tanstack/react-query";
import {
  ArrowRight,
  Award,
  BarChart3,
  Building2,
  Check,
  Coins,
  Database,
  Facebook,
  Globe2,
  Linkedin,
  Mail,
  MapPin,
  Phone,
  ShieldCheck,
  Target,
  Users,
} from "lucide-react";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { api } from "@/lib/api/client";

export const Route = createFileRoute("/")({
  head: () => ({
    meta: [
      { title: "Datasphere — Find verified B2B decision-makers" },
      {
        name: "description",
        content:
          "Search a verified database of importers, exporters, manufacturers and distributors, then unlock direct phone numbers and emails with tokens.",
      },
      { property: "og:title", content: "Datasphere — Find verified B2B decision-makers" },
      {
        property: "og:description",
        content: "Verified company and contact data for sales, sourcing and market research teams.",
      },
    ],
  }),
  component: Landing,
});

const features = [
  {
    icon: Target,
    title: "Sales prospecting",
    body: "Build targeted lists of decision-makers and reach them directly, without gatekeepers.",
  },
  {
    icon: BarChart3,
    title: "Market research",
    body: "Map an industry by country, company type and size before you commit budget.",
  },
  {
    icon: Users,
    title: "Lead generation",
    body: "Turn filtered searches into qualified leads with verified contact details.",
  },
  {
    icon: Globe2,
    title: "Vendor sourcing",
    body: "Find manufacturers and distributors that actually ship to your market.",
  },
];

const contactLinks = [
  { label: "Location",
     detail: "10 Ahmed Mekhamer, El-Nozha, Cairo, Egypt",
     target: "_blank",
     href: "https://maps.app.goo.gl/bJT8SwDx5VJRfTDw6",
     icon: MapPin },

  { label: "Phone",
    detail: "+20 1030034760",
    target: "_blank",
    href: "tel:+201030034760",
    icon: Phone },
  { label: "Email", detail: "info@datainfo.com", target: "_blank", href: "mailto:info@datainfo.com", icon: Mail },
  { label: "LinkedIn", detail: "linkedin.com/company/dataninfo/", target: "_blank", href: "https://linkedin.com/company/dataninfo/", icon: Linkedin },
  { label: "Facebook", detail: "https://www.facebook.com/dataninfo/", target: "_blank", href: "https://www.facebook.com/dataninfo/", icon: Facebook },
];

const stats = [
  {
    icon: Building2,
    value: 78000,
    format: (n: number) => `${Math.round(n).toLocaleString("en-US")}+`,
    label: "Companies indexed",
  },
  {
    icon: Users,
    value: 2.1,
    format: (n: number) => `${n.toFixed(1)}M`,
    label: "Verified contacts",
  },
  {
    icon: Award,
    value: 35,
    format: (n: number) => `+${Math.round(n)}`,
    label: "Years in business",
  },
  {
    icon: Globe2,
    value: 64,
    format: (n: number) => `+${Math.round(n)}`,
    label: "Countries covered",
  },
];

function Landing() {
  const packages = useQuery({ queryKey: ["packages"], queryFn: api.packages });

  useEffect(() => {
    const targets = document.querySelectorAll<HTMLElement>("[data-scroll-reveal]");
    const reduceMotion = window.matchMedia("(prefers-reduced-motion: reduce)").matches;

    if (reduceMotion || !("IntersectionObserver" in window)) {
      targets.forEach((target) => target.setAttribute("data-revealed", "true"));
      return;
    }

    document.documentElement.classList.add("scroll-reveal-enabled");
    const observer = new IntersectionObserver(
      (entries, currentObserver) => {
        entries.forEach((entry) => {
          if (entry.isIntersecting) {
            entry.target.setAttribute("data-revealed", "true");
            currentObserver.unobserve(entry.target);
          }
        });
      },
      { threshold: 0.12 },
    );

    targets.forEach((target) => observer.observe(target));
    return () => {
      observer.disconnect();
      document.documentElement.classList.remove("scroll-reveal-enabled");
    };
  }, []);

  return (
    <div className="min-h-screen bg-background">
      <header className="sticky top-0 z-40 border-b border-border/70 bg-background/85 backdrop-blur">
        <div className="mx-auto flex h-16 max-w-6xl items-center justify-between px-5">
          <Link to="/" className="flex items-center gap-2 font-semibold tracking-tight">
            <span className="grid size-8 place-items-center rounded-lg bg-primary text-primary-foreground">
              <Database className="size-4" />
            </span>
            Datasphere
          </Link>
          <nav className="hidden items-center gap-7 text-sm text-muted-foreground md:flex">
            <a href="#about" className="transition-colors hover:text-foreground">
              About us
            </a>
            <a href="#solutions" className="transition-colors hover:text-foreground">
              Solutions
            </a>
            <a href="#features" className="transition-colors hover:text-foreground">
              Why us
            </a>
            <a href="#pricing" className="transition-colors hover:text-foreground">
              Pricing
            </a>
          </nav>
          <div className="flex items-center gap-2">
            <Button asChild variant="ghost" size="sm">
              <Link to="/login">Log in</Link>
            </Button>
            <Button asChild size="sm">
              <Link to="/signup">Sign up</Link>
            </Button>
          </div>
        </div>
      </header>

      <section className="group relative overflow-hidden transition-shadow duration-500 hover:shadow-[0_30px_90px_-30px_oklch(0.51_0.19_262/28%)]">
        <div className="pointer-events-none absolute inset-x-0 -top-40 h-80 bg-[radial-gradient(60%_60%_at_50%_50%,var(--color-accent),transparent)] opacity-70 transition-opacity duration-500 group-hover:opacity-100" />
        <div data-scroll-reveal className="relative mx-auto max-w-6xl px-5 pt-20 pb-16 text-center md:pt-28">

          <h1 className="mx-auto max-w-3xl text-4xl font-bold text-balance md:text-6xl">
            Find verified contact info for decision-makers at any company
          </h1>
          <p className="mx-auto mt-5 max-w-2xl text-lg text-muted-foreground text-pretty">
            Search importers, exporters, manufacturers and distributors worldwide. Spend tokens only on
            the phone numbers and emails you actually need.
          </p>
          <div className="mt-8 flex flex-wrap justify-center gap-3">
            <Button asChild size="lg">
              <Link to="/signup">
                Get started <ArrowRight className="size-4" />
              </Link>
            </Button>
            <Button asChild size="lg" variant="outline">
              <a href="#pricing">See pricing</a>
            </Button>
          </div>
        </div>
      </section>

      <section aria-label="Platform statistics" className="border-y border-border bg-surface">
        <div className="mx-auto grid max-w-5xl grid-cols-2 gap-px overflow-hidden bg-border text-left sm:grid-cols-4">
          {stats.map((stat) => (
            <div key={stat.label} className="bg-card px-6 py-7">
              <stat.icon className="size-5 text-primary" />
              <p className="mt-3 text-3xl font-semibold tabular-nums">
                <CountUp value={stat.value} format={stat.format} />
              </p>
              <p className="text-sm text-muted-foreground">{stat.label}</p>
            </div>
          ))}
        </div>
      </section>

      <section id="about" className="border-t border-border bg-surface py-20">
        <div data-scroll-reveal className="mx-auto grid max-w-6xl gap-12 px-5 lg:grid-cols-[1fr_1.4fr]">
          <div>
            <h2 className="text-3xl font-semibold text-balance md:text-4xl">
              Technology-driven construction intelligence
            </h2>
            <p className="mt-4 text-muted-foreground">
              Datainfo is the authorized Partner of Ventures Onsite in Egypt.
            </p>
            <div className="mt-8 flex items-start gap-3 rounded-2xl border border-border/80 bg-card p-5 shadow-[var(--shadow-card)]">
              <span className="mt-0.5 grid size-9 shrink-0 place-items-center rounded-xl bg-accent text-accent-foreground">
                <Globe2 className="size-4" />
              </span>
              <div>
                <p className="text-sm font-semibold">Official partnership</p>
                <p className="mt-1 text-sm text-muted-foreground">
                  As the authorized Partner of Ventures Onsite in Egypt, we bring regional project
                  intelligence to a global standard.
                </p>
              </div>
            </div>
          </div>
          <div className="space-y-5 text-muted-foreground">
            <p>
              Datainfo is an expert technology-driven company with the main focus on providing
              construction projects information, market research, analytics services, and insights
              on the construction industry that help companies plan short- and long-term growth
              strategies.
            </p>
            <p>
              Datainfo is transforming the way construction professionals discover and connect with
              the projects, people, and products that drive today's construction industry. We help
              building product manufacturers and distributors, contractors, subcontractors,
              architects, and construction services providers identify, prioritize and act on key
              project opportunities and relationships.
            </p>
          </div>
        </div>
      </section>

      <section id="solutions" className="border-t border-border py-20">
        <div data-scroll-reveal className="mx-auto max-w-6xl px-5">
          <div className="text-center">
            <h2 className="text-3xl font-semibold text-balance md:text-4xl">
              One platform, every solution
            </h2>
            <p className="mx-auto mt-3 max-w-2xl text-muted-foreground">
              Everything you need to go from an idea to a real conversation with the right
              decision-maker.
            </p>
          </div>
          <div className="mt-12 grid gap-5 sm:grid-cols-2 lg:grid-cols-4">
            {[
              {
                icon: Building2,
                title: "Company Hub",
                body: "Search importers, exporters, manufacturers and distributors with filters for industry, country and company type.",
              },
              {
                icon: Users,
                title: "People Hub",
                body: "Find the decision-makers inside those companies by name, role, department and location.",
              },
              {
                icon: ShieldCheck,
                title: "Verified contact details",
                body: "Unlock mobile numbers, direct lines, emails and LinkedIn profiles — each with its last-verified date.",
              },
              {
                icon: Coins,
                title: "Token-based pricing",
                body: "No subscriptions you can't control. Spend tokens only on the details you actually need; they never expire.",
              },
            ].map((s) => (
              <Card
                key={s.title}
                className="border-border/80 shadow-[var(--shadow-card)] transition-[translate,box-shadow,border-color] duration-500 ease-[cubic-bezier(0.22,1,0.36,1)] hover:-translate-y-1 hover:border-primary/30 hover:shadow-[var(--shadow-lift)]"
              >
                <CardHeader>
                  <span className="grid size-10 place-items-center rounded-xl bg-accent text-accent-foreground">
                    <s.icon className="size-5" />
                  </span>
                  <CardTitle className="pt-2 text-base">{s.title}</CardTitle>
                </CardHeader>
                <CardContent className="text-sm text-muted-foreground">
                  {s.title === "Verified contact details"
                    ? "Unlock mobile numbers, direct lines, emails and LinkedIn profiles with verification dates from the database."
                    : s.title === "Token-based pricing"
                      ? "Spend tokens on the contact details you need; each listing shows its current database cost."
                      : s.body}
                </CardContent>
              </Card>
            ))}
          </div>
          <div className="mt-10 text-center">
            <Button asChild size="lg">
              <Link to="/signup">
                Explore the platform <ArrowRight className="size-4" />
              </Link>
            </Button>
          </div>
        </div>
      </section>

      <section id="features" className="border-t border-border bg-surface py-20">
        <div data-scroll-reveal className="mx-auto max-w-6xl px-5">
          <h2 className="max-w-xl text-3xl font-semibold md:text-4xl">Built for businesses that live on data</h2>
          <p className="mt-3 max-w-xl text-muted-foreground">
            One database, four ways to use it
          </p>
          <div className="mt-10 grid gap-5 sm:grid-cols-2 lg:grid-cols-4">
            {features.map((f) => (
              <Card
                key={f.title}
                className="border-border/80 shadow-[var(--shadow-card)] transition-[translate,box-shadow,border-color] duration-500 ease-[cubic-bezier(0.22,1,0.36,1)] hover:-translate-y-1 hover:border-primary/30 hover:shadow-[var(--shadow-lift)]"
              >
                <CardHeader>
                  <span className="grid size-10 place-items-center rounded-xl bg-accent text-accent-foreground">
                    <f.icon className="size-5" />
                  </span>
                  <CardTitle className="pt-2 text-base">{f.title}</CardTitle>
                </CardHeader>
                <CardContent className="text-sm text-muted-foreground">{f.body}</CardContent>
              </Card>
            ))}
          </div>
        </div>
      </section>

      <section id="pricing" className="py-20">
        <div data-scroll-reveal className="mx-auto max-w-6xl px-5">
          <div className="text-center">
            <h2 className="text-3xl font-semibold md:text-4xl">Pay only for what you unlock</h2>
            <p className="mt-3 text-muted-foreground">
              Tokens never expire. One contact detail costs ~5 tokens.
            </p>
          </div>
          <div className="mt-10 grid gap-6 md:grid-cols-3">
            {packages.isLoading ? (
              <p className="text-sm text-muted-foreground">Loading active packages…</p>
            ) : packages.isError ? (
              <p className="text-sm text-destructive">Packages are temporarily unavailable.</p>
            ) : !packages.data?.length ? (
              <p className="text-sm text-muted-foreground">No active packages are configured.</p>
            ) : packages.data.map((item) => (
              <Card
                key={item.id}
                className={item.badge
                  ? "relative border-2 border-primary bg-primary/5 shadow-[var(--shadow-lift)]"
                  : "border-border/80 shadow-[var(--shadow-card)]"}
              >
                {item.badge && <Badge className="absolute -top-3 left-5">{item.badge}</Badge>}
                <CardHeader className={item.badge ? "pt-8" : undefined}>
                  <CardTitle className="text-base font-medium text-muted-foreground">{item.name}</CardTitle>
                  <p className="text-4xl font-semibold">${item.price.toLocaleString("en-US")}</p>
                  <p className="text-sm text-muted-foreground">{item.tokens.toLocaleString("en-US")} tokens</p>
                </CardHeader>
                <CardContent className="space-y-5">
                  {item.features.length > 0 && (
                    <ul className="space-y-2 text-sm">
                      {item.features.map((feature) => (
                        <li key={feature} className="flex items-start gap-2">
                          <Check className="mt-0.5 size-4 shrink-0 text-success" />
                          <span>{feature}</span>
                        </li>
                      ))}
                    </ul>
                  )}
                  <Button asChild className="w-full" variant={item.badge ? "default" : "outline"}>
                    <Link to="/signup">Create account</Link>
                  </Button>
                </CardContent>
              </Card>
            ))}
          </div>
        </div>
      </section>

      <footer className="border-t border-border bg-surface py-10">
        <div className="mx-auto max-w-6xl px-5">
          <div data-scroll-reveal className="border-b border-border pb-8">
            <h2 className="text-lg font-semibold text-foreground">Contact</h2>
            <div className="mt-5 grid gap-x-6 gap-y-5 sm:grid-cols-2 lg:grid-cols-3">
              {contactLinks.map((contact) => (
                <a
                  key={contact.label}
                  href={contact.href}
                  aria-label={`${contact.label}: ${contact.detail}`}
                  className="flex min-w-0 items-center gap-3 text-muted-foreground transition-colors hover:text-foreground"
                >
                  <contact.icon className="size-5 shrink-0 text-primary" />
                  <span className="min-w-0">
                    <span className="block text-sm font-medium text-foreground">{contact.label}</span>
                    <span className="block truncate text-sm">{contact.detail}</span>
                  </span>
                </a>
              ))}
            </div>
          </div>
          <div className="flex flex-col items-center justify-between gap-4 pt-6 text-sm text-muted-foreground sm:flex-row">
            <div className="flex items-center gap-2 font-semibold text-foreground">
              <span className="grid size-7 place-items-center rounded-lg bg-primary text-primary-foreground">
                <Database className="size-3.5" />
              </span>
              Datasphere
            </div>
            <p>© {new Date().getFullYear()} Datasphere. All rights reserved.</p>
            <div className="flex gap-5">
              <a href="#features" className="hover:text-foreground">
                Use cases
              </a>
              <a href="#pricing" className="hover:text-foreground">
                Pricing
              </a>
              <Link to="/login" className="hover:text-foreground">
                Log in
              </Link>
            </div>
          </div>
        </div>
      </footer>
    </div>
  );
}

function CountUp({ value, format }: { value: number; format: (n: number) => string }) {
  const [display, setDisplay] = useState(0);

  useEffect(() => {
    const start = performance.now();
    const duration = 1200;
    let frame = 0;

    function tick(now: number) {
      const progress = Math.min(1, (now - start) / duration);
      const eased = 1 - (1 - progress) ** 3;
      setDisplay(value * eased);
      if (progress < 1) frame = requestAnimationFrame(tick);
    }

    frame = requestAnimationFrame(tick);
    return () => cancelAnimationFrame(frame);
  }, [value]);

  return <>{format(display)}</>;
}
