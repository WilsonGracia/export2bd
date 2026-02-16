import * as XLSX from 'xlsx';

//! generated helper functions for validating the structure of the uploaded Excel file for controls.

const REQUIRED_COLUMNS = ['id_number', 'name', 'type', 'description'];

//! extracts headers from the first row of the worksheet and normalizes
export function extractHeaders(worksheet: XLSX.WorkSheet): string[] {
  const range = XLSX.utils.decode_range(worksheet['!ref'] ?? 'A1');
  const headers: string[] = [];

  for (let col = range.s.c; col <= range.e.c; col++) {
    const cellAddress = XLSX.utils.encode_cell({ r: range.s.r, c: col });
    const cell = worksheet[cellAddress];
    headers.push(cell ? String(cell.v).trim().toLowerCase() : '');
  }

  return headers;
}

//! checks if the required columns are present and if there are any extra columns that are not recognized.
export function validateControlHeaders(headers: string[]): string | null {
  const missing = REQUIRED_COLUMNS.filter((col) => !headers.includes(col));
  const extra = headers.filter((h) => h && !REQUIRED_COLUMNS.includes(h));

  if (missing.length > 0) {
    return (
      `The file does not have the correct structure. ` +
      `Missing columns: ${missing.join(', ')}`
    );
  }

  if (extra.length > 0) {
    return (
      `The file contains unrecognized columns: ${extra.join(', ')}. ` +
      `Only allowed: ${REQUIRED_COLUMNS.join(', ')}`
    );
  }

  return null;
}
