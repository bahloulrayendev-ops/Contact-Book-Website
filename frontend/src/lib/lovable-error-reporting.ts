type LovableErrorContext = Record<string, unknown>;

export function reportLovableError(error: unknown, context?: LovableErrorContext) {
  if (typeof console !== "undefined" && typeof console.error === "function") {
    console.error("[Lovable error]", error, context ?? {});
  }
}

export const reportError = reportLovableError;

export default {
  reportLovableError,
  reportError,
};
