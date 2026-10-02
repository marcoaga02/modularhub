import {Language} from 'app/core/api/models/language';
import {UUID} from 'app/core/api/models/uuid';

export interface AccountPreferences {
  language: Language
}

export interface AccountPreferencesRequest {
  languageId: UUID
}
