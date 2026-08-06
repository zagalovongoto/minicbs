package org.gimuemoa.minicbs.service.impl;

import lombok.RequiredArgsConstructor;
import org.gimuemoa.minicbs.model.SystemParameter;
import org.gimuemoa.minicbs.repository.SystemParameterRepository;
import org.gimuemoa.minicbs.service.SystemParameterService;
import org.gimuemoa.minicbs.exceptions.CustomExceptions.BusinessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Transactional
public class SystemParameterServiceImpl implements SystemParameterService {

    private final SystemParameterRepository parameterRepository;

    @Override
    @Transactional(readOnly = true)
    public String getString(String key, String defaultValue) {
        return parameterRepository.findByParamKey(key)
                .map(SystemParameter::getParamValue)
                .orElse(defaultValue);
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal getBigDecimal(String key, BigDecimal defaultValue) {
        try {
            return parameterRepository.findByParamKey(key)
                    .map(p -> new BigDecimal(p.getParamValue()))
                    .orElse(defaultValue);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    @Override
    public void updateParameter(String key, String value) {
        SystemParameter param = parameterRepository.findByParamKey(key)
                .orElseThrow(() -> new BusinessException("paramKey", "Paramètre système introuvable."));

        if (!param.isEditable()) {
            throw new BusinessException("paramKey", "Ce paramètre système est verrouillé et ne peut pas être modifié.");
        }

        param.setParamValue(value);
        parameterRepository.save(param);
    }

    @Override
    @Transactional(readOnly = true)
    public String getRequiredString(String key) {
        return parameterRepository.findById(key)
                .map(org.gimuemoa.minicbs.model.SystemParameter::getParamValue)
                .orElseThrow(() -> new org.gimuemoa.minicbs.exceptions.CustomExceptions.BusinessException(
                        "paramKey", "Erreur critique : Le paramètre réglementaire [" + key + "] est manquant en base de données."
                ));
    }

}
