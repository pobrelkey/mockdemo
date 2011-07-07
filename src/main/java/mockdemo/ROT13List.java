package mockdemo;

import java.util.AbstractList;
import java.util.List;

public class ROT13List extends AbstractList<String> {
    private final List<String> delegate;

    public ROT13List(List<String> delegate) {
        this.delegate = delegate;
    }

    @Override
    public String get(int index) {
        return rot13(delegate.get(index));
    }

    @Override
    public String set(int index, String element) {
        return rot13(delegate.set(index, rot13(element)));
    }

    @Override
    public void add(int index, String element) {
        delegate.add(index, rot13(element));
    }

    @Override
    public int indexOf(Object o) {
        return delegate.indexOf(rot13((String) o));
    }

    @Override
    public int lastIndexOf(Object o) {
        return delegate.lastIndexOf(rot13((String) o));
    }

    @Override
    public int size() {
        return delegate.size();
    }

    @Override
    public boolean contains(Object o) {
        return delegate.contains(rot13((String) o));
    }

    @Override
    public boolean add(String s) {
        return delegate.add(rot13(s));
    }

    @Override
    public boolean remove(Object o) {
        return delegate.remove(rot13((String) o));
    }

    @Override
    public String remove(int index) {
        return rot13(delegate.remove(index));
    }

    @Override
    public void clear() {
        delegate.clear();
    }

    public static String rot13(String s) {
        if (s == null) {
            return null;
        }
        StringBuilder result = new StringBuilder();
        for (char c : s.toCharArray()) {
            if (c >= 'A' && c <= 'Z') {
                result.append((char) ('A' + ((13 + c - 'A') % 26)));
            } else if (c >= 'a' && c <= 'z') {
                result.append((char) ('a' + ((13 + c - 'a') % 26)));
            } else {
                result.append(c);
            }
        }
        return result.toString();
    }

}
