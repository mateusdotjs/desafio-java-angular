export function positiveId(raw: string | null): number | null {
  if (raw === null) return null;
  const parsed = Number(raw);
  return Number.isInteger(parsed) && parsed > 0 ? parsed : null;
}

export function ownerId(rawUserId: string | null, sessionUserId: number | null): number | null {
  if (rawUserId === null) return sessionUserId;
  return positiveId(rawUserId);
}
