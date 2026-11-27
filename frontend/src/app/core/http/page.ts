export interface Page<T> {
  content: T[];
  empty: boolean;
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
}
