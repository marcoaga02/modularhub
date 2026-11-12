import {LanguageResponse} from 'app/core/api/generated';
import {UUID} from 'app/core/api/models/uuid';

export interface AccountPreferences {
  language: LanguageResponse
}

export interface AccountPreferencesRequest {
  languageId: UUID
}
