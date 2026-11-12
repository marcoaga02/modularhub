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

  get page(): number | undefined {
    return this.limit ? Math.floor((this.offset ?? 0) / this.limit) : undefined;
  }

  get size(): number | undefined {
    return this.limit || undefined;
  }

  public override toParams(): HttpParams {
    let params = super.toParams();

    if (this.page !== undefined && this.size !== undefined) {
      params = params.append('page', String(this.page));
      params = params.append('size', String(this.size));
    }

    if (this.sort) {
      params = params.append('sort', this.sort);
    }

    return params;
  }
}
