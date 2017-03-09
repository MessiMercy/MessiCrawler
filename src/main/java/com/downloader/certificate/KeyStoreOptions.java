package com.downloader.certificate;

/**
 * Created by Administrator on 2016/11/3.
 */
public class KeyStoreOptions {
    private final String type;
    private final String path;
    private final String password;

    public KeyStoreOptions(String type, String path, String password) {
        super();
        this.type = type;
        this.path = path;
        this.password = password;
    }

    public String getType() {
        return type;
    }

    public String getPath() {
        return path;
    }

    public String getPassword() {
        return password;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((password == null) ? 0 : password.hashCode());
        result = prime * result + ((path == null) ? 0 : path.hashCode());
        result = prime * result + ((type == null) ? 0 : type.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        KeyStoreOptions other = (KeyStoreOptions) obj;
        if (password == null) {
            if (other.password != null)
                return false;
        } else if (!password.equals(other.password))
            return false;
        if (path == null) {
            if (other.path != null)
                return false;
        } else if (!path.equals(other.path))
            return false;
        if (type == null) {
            if (other.type != null)
                return false;
        } else if (!type.equals(other.type))
            return false;
        return true;
    }
}
