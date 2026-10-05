export function formatDate(iso: string) {
  return new Date(iso).toLocaleDateString(undefined, {
    year: "numeric",
    month: "short",
    day: "numeric",
  });
}

export function formatDateTime(iso: string) {
  return new Date(iso).toLocaleString(undefined, {
    month: "short",
    day: "numeric",
    hour: "2-digit",
    minute: "2-digit",
  });
}

export function titleCase(value: string) {
  return value.charAt(0) + value.slice(1).toLowerCase();
}

export function formatRelativeVerification(iso: string, now = new Date()) {
  const verifiedAt = new Date(iso);
  if (Number.isNaN(verifiedAt.getTime())) return "Verified recently";

  const days = Math.max(0, Math.floor((now.getTime() - verifiedAt.getTime()) / 86_400_000));
  if (days === 0) return "Verified today";
  if (days === 1) return "Verified yesterday";
  if (days < 7) return `Verified ${days} days ago`;

  const weeks = Math.floor(days / 7);
  if (days < 30) return `Verified ${weeks} ${weeks === 1 ? "week" : "weeks"} ago`;

  const months = Math.floor(days / 30);
  if (days < 365) return `Verified ${months} ${months === 1 ? "month" : "months"} ago`;

  const years = Math.floor(days / 365);
  return `Verified ${years} ${years === 1 ? "year" : "years"} ago`;
}
