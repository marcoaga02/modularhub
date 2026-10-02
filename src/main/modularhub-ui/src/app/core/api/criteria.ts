import {HttpParams} from '@angular/common/http';

export abstract class Criteria {
  filter: any;

  public toParams(): HttpParams {
    let params = new HttpParams();

    if (!this.filter) {
      return params;
    }

    for (const [key, value] of Object.entries(this.filter)) {
      if (value === null || value === undefined) {
        continue;
      }

      if (Array.isArray(value)) {
        params = params.appendAll({[key]: value.map(String)});
      } else {
        params = params.append(key, String(value));
      }
    }

    return params;
  }
}

export abstract class PagedCriteria extends Criteria {
  offset: number;
  limit: number;
  sort: string;

  protected constructor(offset: number, limit: number, sort: string) {
    super();
    this.offset = offset;
    this.limit = limit;
    this.sort = sort;
  }

  public override toParams(): HttpParams {
    let params = super.toParams();

    if (this.offset != null) {
      params = params.append('offset', String(this.offset));
    }

    if (this.limit != null) {
      params = params.append('limit', String(this.limit));
    }

    if (this.sort) {
      params = params.append('sort', this.sort);
    }

    return params;
  }
}
