package org.gimuemoa.minicbs.service;

import java.math.BigDecimal;

public interface SystemParameterService {
    String getString(String key, String defaultValue);
    BigDecimal getBigDecimal(String key, BigDecimal defaultValue);
    void updateParameter(String key, String value);
    String getRequiredString(String key);
}
