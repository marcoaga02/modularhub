import { DynamicDialogConfig } from 'primeng/dynamicdialog';

export const FORM_DIALOG_CONFIG = {
  showHeader: false,
  width: '44rem',
  style: { maxWidth: '95vw', overflow: 'hidden' },
  contentStyle: {
    padding: '0',
    overflow: 'hidden',
    display: 'flex',
    flexDirection: 'column',
  },
} as const satisfies DynamicDialogConfig;
