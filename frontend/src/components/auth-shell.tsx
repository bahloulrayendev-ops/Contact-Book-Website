import type { ReactNode } from "react";
import { Link } from "@tanstack/react-router";
import { ArrowLeft, Database } from "lucide-react";

export function AuthShell({ children }: { children: ReactNode }) {
  return (
    <main className="relative flex min-h-screen flex-col bg-background lg:grid lg:grid-cols-[minmax(0,0.9fr)_minmax(0,1.1fr)]">
      <Link
        to="/"
        className="absolute left-4 top-4 z-30 inline-flex items-center gap-2 rounded-md border border-border/70 bg-background/90 px-3 py-2 text-sm font-medium text-foreground shadow-[var(--shadow-card)] transition-colors hover:bg-accent hover:text-accent-foreground"
      >
        <ArrowLeft className="size-4" />
        Back to home
      </Link>
      <section className="order-2 flex flex-1 items-center justify-center px-5 py-10 sm:px-8 lg:order-1 lg:min-h-screen lg:px-10 xl:px-16">
        <div className="w-full max-w-sm">
          <Link to="/" className="mb-8 flex items-center justify-center gap-2 font-semibold lg:justify-start">
            <span className="grid size-8 place-items-center rounded-lg bg-primary text-primary-foreground">
              <Database className="size-4" />
            </span>
            Datasphere
          </Link>
          {children}
        </div>
      </section>

      <aside className="order-1 relative isolate flex min-h-[22rem] flex-col overflow-hidden border-b border-border bg-accent/60 px-6 pb-7 pt-16 sm:min-h-[26rem] sm:px-9 lg:order-2 lg:min-h-screen lg:border-b-0 lg:border-l lg:px-12 lg:py-10 xl:px-16">
        <div
          aria-hidden="true"
          className="pointer-events-none absolute inset-0 opacity-30"
          style={{
            backgroundImage:
              "linear-gradient(var(--color-border) 1px, transparent 1px), linear-gradient(90deg, var(--color-border) 1px, transparent 1px)",
            backgroundSize: "48px 48px",
          }}
        />
        <div className="relative z-10 flex flex-1 flex-col">
          <p className="text-xs font-semibold uppercase tracking-[0.12em] text-accent-foreground">
            Datasphere · Global network
          </p>
          <div className="relative mx-auto my-1 h-40 w-full max-w-[34rem] sm:h-52 lg:absolute lg:inset-x-0 lg:top-[10%] lg:my-0 lg:h-[min(58vh,38rem)] lg:max-w-none">
            <NetworkGlobe />
          </div>
          <div className="relative z-10 mt-auto max-w-xl pt-2 lg:pt-0">
            <p className="mb-3 text-sm font-medium text-primary">Business is built on the right connections</p>
            <h1 className="max-w-lg text-3xl font-semibold leading-tight text-foreground sm:text-4xl xl:text-5xl">
              Meet the people moving business forward.
            </h1>
            <p className="mt-4 max-w-lg text-sm leading-relaxed text-muted-foreground sm:text-base">
              Connect with verified decision-makers across the world. Find the right companies, reach the right people, and start meaningful conversations with Datasphere.
            </p>
          </div>
        </div>
      </aside>
    </main>
  );
}

function NetworkGlobe() {
  return (
    <svg
      viewBox="0 0 640 500"
      role="img"
      aria-label="Light-blue wireframe globe with connected business locations"
      className="h-full w-full overflow-visible text-primary"
      fill="none"
    >
      <defs>
        <clipPath id="network-globe-clip">
          <circle cx="320" cy="250" r="174" />
        </clipPath>
      </defs>
      <ellipse
        cx="320"
        cy="250"
        rx="244"
        ry="126"
        transform="rotate(-16 320 250)"
        stroke="currentColor"
        strokeOpacity=".2"
        strokeWidth="1.5"
      />
      <circle cx="320" cy="250" r="174" fill="currentColor" fillOpacity=".07" stroke="currentColor" strokeOpacity=".34" strokeWidth="2" />
      <g clipPath="url(#network-globe-clip)" stroke="currentColor" strokeOpacity=".27" strokeWidth="1.5">
        <ellipse cx="320" cy="250" rx="72" ry="174" />
        <ellipse cx="320" cy="250" rx="132" ry="174" />
        <path d="M146 250c72-48 276-48 348 0M153 204c77-34 257-34 334 0M153 296c77 34 257 34 334 0M169 158c83-24 219-24 302 0M169 342c83 24 219 24 302 0" />
        <path d="m166 299 74-103 82 38 83-74 69 42M191 151l67 89 86-31 39 84 79 15M202 342l69-55 73 32 72-60 56 10" strokeOpacity=".58" />
        <path d="M121 253h398M320 70v360" strokeOpacity=".15" />
      </g>
      <g fill="var(--color-background)" stroke="currentColor" strokeWidth="2">
        <circle cx="166" cy="299" r="6" />
        <circle cx="240" cy="196" r="5" />
        <circle cx="322" cy="234" r="7" />
        <circle cx="405" cy="160" r="6" />
        <circle cx="474" cy="202" r="5" />
        <circle cx="191" cy="151" r="4" />
        <circle cx="334" cy="209" r="4" />
        <circle cx="436" cy="284" r="5" />
        <circle cx="271" cy="287" r="5" />
        <circle cx="416" cy="319" r="4" />
      </g>
      <g fill="currentColor" fillOpacity=".25">
        <circle cx="77" cy="181" r="3" />
        <circle cx="549" cy="333" r="4" />
        <circle cx="505" cy="105" r="3" />
        <circle cx="129" cy="387" r="4" />
      </g>
    </svg>
  );
}