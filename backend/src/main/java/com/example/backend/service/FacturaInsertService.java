package com.example.backend.service;

import com.example.backend.dto.FacturaInsertDto;
import com.example.backend.sqlserver2.model.Cfg;
import com.example.backend.sqlserver2.model.Fac;
import com.example.backend.sqlserver2.model.Fde;
import com.example.backend.sqlserver2.model.Ter;
import com.example.backend.sqlserver2.model.Gbs;
import com.example.backend.sqlserver2.repository.CfgRepository;
import com.example.backend.sqlserver2.repository.FacRepository;
import com.example.backend.sqlserver2.repository.TerRepository;
import com.example.backend.sqlserver2.repository.GbsRepository;
import com.example.backend.sqlserver2.repository.FdeRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class FacturaInsertService {
    @Autowired
    private TerRepository terRepository;
    @Autowired
    private CfgRepository cfgRepository;
    @Autowired
    private FacRepository facRepository;
    @Autowired
    private GbsRepository gbsRepository;
    @Autowired
    private FdeRepository fdeRepository;

    public NamesResponse insertFacturas(List<FacturaInsertDto> facturas) {
        List<FacturaInfo> savedNames = new ArrayList<>();
        List<FacturaInfo> unsavedNames = new ArrayList<>();
        List<String> missingProviders = new ArrayList<>();
        for (FacturaInsertDto dto : facturas) {
            Ter ter = terRepository.findByENTAndTERNIF(dto.ENT, dto.tercero);
            if (ter == null) {
                missingProviders.add(dto.tercero);
                continue;
            }

            List<Fac> factura = facRepository.findByFACTDCAndFACANNAndFACFAC(dto.FACTDC, dto.FACANN, dto.FACFAC);
            if (!factura.isEmpty()) {
                unsavedNames.add(
                    new FacturaInfo(
                        factura.get(0).getFACANN(),
                        factura.get(0).getFACTDC(),
                        factura.get(0).getFACFAC()
                    )
                );
            } else {
                List<Cfg> cfgList = cfgRepository.findByENTAndEJE(dto.ENT, dto.EJE);
                if (cfgList == null || cfgList.isEmpty()) continue;
                Cfg cfg = cfgList.get(0);

                Integer maxFacnum = facRepository.findMaxFACNUMByENTAndEJE(dto.ENT, dto.EJE);
                int newFacnum = (maxFacnum == null ? 1 : maxFacnum + 1);

                Fac fac = new Fac();
                fac.setENT(dto.ENT);
                fac.setEJE(dto.EJE);
                fac.setFACNUM(newFacnum);
                fac.setTERCOD(ter.getTERCOD());
                fac.setCGECOD(dto.CGECOD);
                fac.setFACIMP(dto.FACIMP);
                fac.setFACIEC(dto.FACIEC);
                fac.setFACIDI(dto.FACIDI);
                fac.setFACTDC(dto.FACTDC);
                fac.setFACANN(dto.FACANN);
                fac.setFACFAC(dto.FACFAC);
                fac.setFACDOC(dto.FACDOC);
                fac.setFACDAT(dto.FACDAT);
                fac.setFACTXT(dto.FACTXT);
                fac.setFACDTO(dto.FACDTO);
                fac.setFACFRE(dto.FACFRE);
                fac.setFACFPG(cfg.getCFGFPG());
                fac.setFACOPG(cfg.getCFGOPG());
                fac.setFACTPG(cfg.getCFGTPG());
                facRepository.save(fac);

                List<Gbs> gbsRows = gbsRepository.findByENTAndEJEAndCGECOD(dto.ENT, dto.EJE, dto.CGECOD);
                for (Gbs gbs : gbsRows) {
                    Fde fde = new Fde();
                    fde.setENT(dto.ENT);
                    fde.setEJE(dto.EJE);
                    fde.setFACNUM(newFacnum);
                    fde.setFDEREF(gbs.getGBSREF());
                    fde.setFDEOPE(gbs.getGBSOPE());
                    fde.setFDEORG(gbs.getGBSORG());
                    fde.setFDEFUN(gbs.getGBSFUN());
                    fde.setFDEECO(gbs.getGBSECO());
                    fde.setFDESUB(gbs.getGBSSUB());
                    fde.setFDEIMP(0.0);
                    fde.setFDEDIF(0.0);
                    fdeRepository.save(fde);
                }

                savedNames.add(
                    new FacturaInfo(
                        dto.FACANN,
                        dto.FACTDC,
                        dto.FACFAC
                    )
                );
            }
        }
        return new NamesResponse(savedNames, unsavedNames, missingProviders);
    }

    public record NamesResponse (
        List<FacturaInfo> savedNames,
        List<FacturaInfo> unsavedNames,
        List<String> missingProviders
    ) {}

    public record FacturaInfo(
        Integer facann,
        String factdc,
        Integer facfac
    ) {}
}
