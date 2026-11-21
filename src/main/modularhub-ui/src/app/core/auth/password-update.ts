import { Location } from '@angular/common';

export const UPDATE_PASSWORD_ACTION = 'UPDATE_PASSWORD';

const ACTION_PARAM = 'kc_action';
const ACTION_STATUS_PARAM = 'kc_action_status';

export function takePasswordUpdateStatus(location: Location): string | null {
  const [path, query] = location.path().split('?');
  const params = new URLSearchParams(query);
  const status = params.get(ACTION_STATUS_PARAM);

  if (status === null) {
    return null;
  }

  params.delete(ACTION_PARAM);
  params.delete(ACTION_STATUS_PARAM);
  location.replaceState(path, params.toString());

  return status;
}
