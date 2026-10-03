import {UserCriteria, UserSortField} from 'app/modules/user-management/api/user-criteria';

describe('UserCriteria', () => {

  it('should convert offset and limit to page and size', () => {
    const params = new UserCriteria(40, 20).toParams();

    expect(params.get('page')).toBe('2');
    expect(params.get('size')).toBe('20');
    expect(params.has('offset')).toBe(false);
    expect(params.has('limit')).toBe(false);
  });

  it('should return first page when offset is zero', () => {
    const params = new UserCriteria(0, 20).toParams();

    expect(params.get('page')).toBe('0');
    expect(params.get('size')).toBe('20');
  });

  it('should not send page and size when limit is zero', () => {
    const params = new UserCriteria(0, 0).toParams();

    expect(params.has('page')).toBe(false);
    expect(params.has('size')).toBe(false);
  });

  it('should send sort and filter text', () => {
    const params = new UserCriteria(0, 20, {text: 'mario'}, UserSortField.Firstname).toParams();

    expect(params.get('sort')).toBe('firstname');
    expect(params.get('text')).toBe('mario');
  });

  it('should skip undefined filter values', () => {
    const params = new UserCriteria(0, 20, {text: undefined}).toParams();

    expect(params.has('text')).toBe(false);
  });
});
