export type ContactFieldType = "MOBILE" | "DIRECT_LINE" | "EMAIL" | "LINKEDIN";

export interface User {
  id: string;
  fullName: string;
  email: string;
  phone?: string | undefined;
}

export interface AuthResponse {
  token: string;
}

export interface Wallet {
  balance: number;
  totalSpent: number;
  totalPurchased: number;
}

export interface Transaction {
  id: string;
  type: "PURCHASE" | "UNLOCK" | "BONUS";
  amount: number;
  createdAt: string;
  balanceAfter: number;
  description: string;
}

export interface TokenPackage {
  id: string;
  name: string;
  tokens: number;
  price: number;
  features: string[];
  badge: string | null;
}

export interface ContactDetail {
  id: string;
  type: ContactFieldType;
  masked: string;
  value: string | null;
  cost: number;
  unlocked: boolean;
  lastVerifiedAt: string;
}

export interface Employee {
  id: string;
  fullName: string;
  title: string;
  department: string;
  companyId: string;
  companyName: string;
  companyIndustry?: string;
  companyCountry?: string;
  companyType?: CompanyType;
  contactDetails: ContactDetail[];
}

export type CompanyType = "IMPORTER" | "EXPORTER" | "MANUFACTURER" | "DISTRIBUTOR";

export interface Company {
  id: string;
  name: string;
  industry: string;
  country: string;
  city: string;
  type: CompanyType;
  website: string;
  employeeCount: number;
  verified: boolean;
  foundedYear: number;
  geographicPresence: string[];
  description: string;
}

export interface Page<T> {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
}

export interface SearchFilters {
  keyword?: string;
  industry?: string;
  country?: string;
  companyType?: string;
  page?: number;
  size?: number;
}

export interface UnlockRecord {
  id: string;
  employeeName: string;
  companyName: string;
  fieldType: ContactFieldType;
  value: string;
  tokensSpent: number;
  unlockedAt: string;
}

export interface SearchRecord {
  id: string;
  type: "company" | "people";
  query: string;
  filters: string;
  resultCount: number;
  searchedAt: string;
}

export interface CompanyFilters {
  industries: string[];
  countries: string[];
  types: CompanyType[];
}

export interface PeopleFilters {
  titles: string[];
  departments: string[];
  industries: string[];
  countries: string[];
}

export interface PackagePurchase {
  purchaseId: number;
  status: string;
  packageName: string;
  price: number;
  tokens: number;
  purchaseDate: string;
}

export interface ContactUpdateNotification {
  id: number;
  contactDetailId: number;
  title: string;
  message: string;
  contactType: "mobile" | "direct_line" | "email" | "linkedin";
  newValue: string | null;
  discountedTokenCost: number | null;
  read: boolean;
  repurchaseCompleted: boolean;
  createdAt: string;
}

export class ApiError extends Error {
  status: number;
  constructor(message: string, status = 500) {
    super(message);
    this.name = "ApiError";
    this.status = status;
  }
}
