export interface Address {
  id: number;
  street: string;
  number: string;
  complement: string | null;
  neighborhood: string;
  city: string;
  state: string;
  zipCode: string;
  userId: number;
}

export interface AddressRequest {
  zipCode: string;
  number: string;
  complement: string | null;
}

export interface ZipCodeLookup {
  zipCode: string;
  street: string;
  neighborhood: string;
  city: string;
  state: string;
}
