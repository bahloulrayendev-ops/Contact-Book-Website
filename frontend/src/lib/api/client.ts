import { API_BASE_URL, TOKEN_STORAGE_KEY } from "./config";
import {
  ApiError,
  type AuthResponse,
  type Company,
  type CompanyFilters,
  type Employee,
  type Page,
  type PackagePurchase,
  type PeopleFilters,
  type SearchFilters,
  type SearchRecord,
  type TokenPackage,
  type Transaction,
  type UnlockRecord,
  type User,
  type Wallet,
  type ContactDetail,
  type ContactUpdateNotification,
} from "./types";

export function getToken(): string | null {
  if (typeof window === "undefined") return null;
  return window.localStorage.getItem(TOKEN_STORAGE_KEY);
}

export function setToken(token: string | null) {
  if (typeof window === "undefined") return;
  if (token) window.localStorage.setItem(TOKEN_STORAGE_KEY, token);
  else window.localStorage.removeItem(TOKEN_STORAGE_KEY);
}

type Query = Record<string, string | number | undefined>;

interface SpringPage<T> {
  content: T[];
  number?: number;
  page?: number;
  size: number;
  totalElements: number;
  totalPages: number;
}

interface SpringContact {
  id: number;
  contactType: string;
  tokenCost: number;
  lastVerified: string;
  locked: boolean;
  value: string | null;
}

interface SpringEmployee {
  id: number;
  firstName: string;
  lastName: string;
  jobTitle: string;
  department: string;
  contactDetails: SpringContact[];
  company?: { id: number; name: string; country: string; industry: string };
}

interface SpringCompany {
  id: number;
  name: string;
  description?: string;
  industry: string;
  city: string;
  country: string;
  website: string;
  linkedin?: string;
  companyType: Company["type"];
  employeeCount?: number;
  estabYear?: number;
  geographicPresence?: string[];
}

function normalizePage<T, U>(page: SpringPage<T>, map: (item: T) => U): Page<U> {
  return {
    content: page.content.map(map),
    page: page.number ?? page.page ?? 0,
    size: page.size,
    totalElements: page.totalElements,
    totalPages: page.totalPages,
  };
}

function normalizeContactType(value: string): ContactDetail["type"] {
  return value.toUpperCase() as ContactDetail["type"];
}

function toContact(contact: SpringContact): ContactDetail {
  const type = normalizeContactType(contact.contactType);
  const masked: Record<ContactDetail["type"], string> = {
    MOBILE: "••••••••",
    DIRECT_LINE: "••••••••",
    EMAIL: "••••••@••••",
    LINKEDIN: "linkedin.com/in/••••",
  };
  return {
    id: String(contact.id),
    type,
    masked: masked[type],
    value: contact.value,
    cost: contact.tokenCost,
    unlocked: !contact.locked,
    lastVerifiedAt: contact.lastVerified,
  };
}

function toEmployee(employee: SpringEmployee, companyId?: string): Employee {
  const company = employee.company;
  return {
    id: String(employee.id),
    fullName: `${employee.firstName} ${employee.lastName}`.trim(),
    title: employee.jobTitle,
    department: employee.department,
    companyId: company ? String(company.id) : (companyId ?? ""),
    companyName: company?.name ?? "",
    ...(company?.industry ? { companyIndustry: company.industry } : {}),
    ...(company?.country ? { companyCountry: company.country } : {}),
    contactDetails: employee.contactDetails.map(toContact),
  };
}

function toCompany(company: SpringCompany): Company {
  return {
    id: String(company.id),
    name: company.name,
    description: company.description ?? "",
    industry: company.industry,
    city: company.city,
    country: company.country,
    website: company.website,
    type: company.companyType,
    employeeCount: company.employeeCount ?? 0,
    verified: false,
    foundedYear: company.estabYear ?? 0,
    geographicPresence: company.geographicPresence ?? [],
  };
}

async function request<T>(
  method: "GET" | "POST" | "PUT" | "DELETE",
  path: string,
  options: { body?: unknown; query?: Query } = {},
): Promise<T> {
  const url = new URL(API_BASE_URL.replace(/\/$/, "") + path);
  Object.entries(options.query ?? {}).forEach(([k, v]) => {
    if (v !== undefined && v !== "") url.searchParams.set(k, String(v));
  });

  const token = getToken();
  const res = await fetch(url.toString(), {
    method,
    headers: {
      "Content-Type": "application/json",
      ...(token ? { Authorization: `Bearer ${token}` } : {}),
    },
    ...(options.body === undefined ? {} : { body: JSON.stringify(options.body) }),
  });

  if (!res.ok) {
    let message = `Request failed (${res.status})`;
    try {
      const data = (await res.json()) as { message?: string; error?: string; detail?: string };
      message = data.message ?? data.error ?? data.detail ?? message;
    } catch {
      /* non-JSON error body */
    }
    throw new ApiError(message, res.status);
  }

  if (res.status === 204) return undefined as T;
  return (await res.json()) as T;
}

export const api = {
  register: (body: { firstName: string; lastName: string; phone: string; email: string; password: string }) =>
    request<AuthResponse>("POST", "/auth/signup", { body }),
  login: (body: { email: string; password: string }) =>
    request<AuthResponse>("POST", "/auth/login", { body }),

  me: () => request<User>("GET", "/users/me"),
  updateMe: (body: { fullName: string; email: string; phone: string }) =>
    request<User>("PUT", "/users/me", { body }),
    changePassword: (body: { currentPassword: string; newPassword: string }) =>
      request<void>("PUT", "/users/me/password", { body }),

  wallet: () => request<Wallet>("GET", "/wallet"),
  transactions: (page = 0) => request<Page<Transaction>>("GET", "/wallet/transactions", { query: { page } }),
  packages: async () => {
    const packages = await request<{
      id: number;
      name: string;
      price: number;
      tokens: number;
      features?: string[] | null;
      badge?: string | null;
    }[]>("GET", "/tokens/packages");
    return packages.map((item): TokenPackage => ({
      id: String(item.id),
      name: item.name,
      price: item.price,
      tokens: item.tokens,
      features: item.features ?? [],
      badge: item.badge ?? null,
    }));
  },
  purchase: (packageId: string) => request<PackagePurchase>("POST", "/tokens/purchase", {
    body: { tokenPackageId: Number(packageId), provider: "manual" },
  }),

  companyFilters: async (): Promise<CompanyFilters> => {
    const result = await request<{ industries: string[]; countries: string[]; companyTypes: Company["type"][] }>("GET", "/companies/filters");
    return { industries: result.industries, countries: result.countries, types: result.companyTypes };
  },
  peopleFilters: async (): Promise<PeopleFilters> => {
    const result = await request<{ jobTitles: string[]; departments: string[]; industries: string[]; countries: string[] }>("GET", "/employees/filters");
    return { titles: result.jobTitles, departments: result.departments, industries: result.industries, countries: result.countries };
  },
  searchCompanies: async (filters: SearchFilters) => {
    const result = await request<SpringPage<SpringCompany>>("GET", "/companies", { query: { ...filters, size: filters.size ?? 10 } as Query });
    return normalizePage(result, toCompany);
  },
  employeesOfCompany: async (companyId: string) => {
    const result = await request<Omit<SpringEmployee, "company">[]>("GET", `/companies/${companyId}/employees`);
    return result.map((employee) => toEmployee(employee, companyId));
  },
  searchPeople: async (filters: Record<string, string | number | undefined>) => {
    const { title, ...rest } = filters;
    const result = await request<SpringPage<SpringEmployee>>("GET", "/employees", {
      query: { ...rest, jobTitle: title, size: 10 },
    });
    return normalizePage(result, toEmployee);
  },
  company: async (id: string) => toCompany(await request<SpringCompany>("GET", `/companies/${id}`)),

  unlock: async (contactDetailId: string) => {
    const result = await request<{ contactDetailId: number; contactType: string; value: string; tokensSpent: number }>("POST", `/unlock/${contactDetailId}`);
    return {
      id: String(result.contactDetailId),
      type: normalizeContactType(result.contactType),
      masked: result.value,
      value: result.value,
      cost: result.tokensSpent,
      unlocked: true,
      lastVerifiedAt: new Date().toISOString(),
    } satisfies ContactDetail;
  },

  unlockHistory: async (page = 0) => {
    const result = await request<SpringPage<UnlockRecord & { fieldType: string }>>(
      "GET", "/history/unlocks", { query: { page } },
    );
    return normalizePage(result, (item): UnlockRecord => ({
      ...item,
      fieldType: normalizeContactType(item.fieldType),
    }));
  },
  searchHistory: (page = 0) =>
    request<Page<SearchRecord>>("GET", "/history/searches", { query: { page } }),
  notifications: (page = 0) =>
    request<Page<ContactUpdateNotification>>("GET", "/notifications", { query: { page } }),
  unreadNotificationCount: () => request<number>("GET", "/notifications/unread-count"),
  markNotificationRead: (notificationId: number) =>
    request<ContactUpdateNotification>("PUT", `/notifications/${notificationId}/read`),
  repurchaseUpdatedContact: (notificationId: number) =>
    request<ContactUpdateNotification>("POST", `/notifications/${notificationId}/repurchase`),
};
