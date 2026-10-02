export interface CustomError {
  timestamp: string;
  status: number;
  errorCode: string;
  fields?: string[];
}
