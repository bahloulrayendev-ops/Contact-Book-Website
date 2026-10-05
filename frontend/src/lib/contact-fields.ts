import { Linkedin, Mail, Phone, Smartphone, type LucideIcon } from "lucide-react";
import type { ContactFieldType } from "@/lib/api/types";

export const CONTACT_FIELD_META: Record<ContactFieldType, { label: string; noun: string; icon: LucideIcon }> = {
  MOBILE: { label: "Mobile", noun: "mobile number", icon: Smartphone },
  DIRECT_LINE: { label: "Direct line", noun: "direct line", icon: Phone },
  EMAIL: { label: "Email", noun: "email address", icon: Mail },
  LINKEDIN: { label: "LinkedIn", noun: "LinkedIn profile", icon: Linkedin },
};

export function contactFieldLabel(type: ContactFieldType): string {
  return CONTACT_FIELD_META[type]?.label ?? type;
}
