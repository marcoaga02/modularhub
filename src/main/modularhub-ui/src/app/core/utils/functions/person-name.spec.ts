import { getFullName, getInitials } from 'app/core/utils/functions/person-name';

describe('person name', () => {
  describe('getFullName', () => {
    it('should join first name and last name', () => {
      expect(getFullName('mario', 'rossi')).toBe('mario rossi');
    });
  });

  describe('getInitials', () => {
    it('should return the upper case initials of first name and last name', () => {
      expect(getInitials('mario', 'rossi')).toBe('MR');
    });

    it('should skip the missing initials of empty names', () => {
      expect(getInitials('', 'rossi')).toBe('R');
    });
  });
});
