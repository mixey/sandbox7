package com.sandbox7.net;

import java.util.List;

public interface RequestCallback {
    void onSuccess(List response);
    void onFail(Object response);
}
