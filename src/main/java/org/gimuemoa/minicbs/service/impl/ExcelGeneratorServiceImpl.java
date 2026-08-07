package org.gimuemoa.minicbs.service.impl;

import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.gimuemoa.minicbs.dto.BankTransactionDTO;
import org.gimuemoa.minicbs.service.ExcelGeneratorService;
import org.gimuemoa.minicbs.service.SystemParameterService;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ExcelGeneratorServiceImpl implements ExcelGeneratorService {

    private final SystemParameterService paramService;

    @Override
    public ByteArrayInputStream generateAccountStatementExcel(String accountNumber, List<BankTransactionDTO> transactions) {
        String bankName = paramService.getRequiredString("BANK_DISPLAY_NAME");

        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Extrait de Compte");

            // 1. CRÉATION DES STYLES DE CELLULES
            // Style de l'en-tête (Gras, fond bleu marine, texte blanc)
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setColor(IndexedColors.WHITE.getIndex());

            CellStyle headerStyle = workbook.createCellStyle();
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);

            // Style pour les montants financiers (Format monétaire)
            CellStyle amountStyle = workbook.createCellStyle();
            DataFormat format = workbook.createDataFormat();
            amountStyle.setDataFormat(format.getFormat("#,##0")); // Format entier sans décimale pour le XOF
            amountStyle.setAlignment(HorizontalAlignment.RIGHT);

            // 2. EN-TÊTE INSTITUTIONNEL
            Row bankRow = sheet.createRow(0);
            Cell bankCell = bankRow.createCell(0);
            bankCell.setCellValue(bankName.toUpperCase() + " - EXTRAIT DE COMPTE");

            Row accountRow = sheet.createRow(1);
            accountRow.createCell(0).setCellValue("Numéro de Compte (IBAN) :");
            accountRow.createCell(1).setCellValue(accountNumber);

            // 3. EN-TÊTE DU TABLEAU COMPTABLE (Ligne 4)
            String[] columns = {"Date Opération", "Référence GIM", "Type", "Compte Débiteur", "Compte Créditeur", "Montant Mouvementé"};
            Row headerRow = sheet.createRow(3);
            for (int i = 0; i < columns.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(columns[i]);
                cell.setCellStyle(headerStyle);
            }

            // 4. INJECTION DES ÉCRITURES COMPTABLES
            int rowIdx = 4;
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

            for (BankTransactionDTO tx : transactions) {
                Row row = sheet.createRow(rowIdx++);

                row.createCell(0).setCellValue(tx.getExecutedAt() != null ? tx.getExecutedAt().format(formatter) : "");
                row.createCell(1).setCellValue(tx.getReference());
                row.createCell(2).setCellValue(tx.getType());
                row.createCell(3).setCellValue(tx.getSourceDbtrAcct() != null ? tx.getSourceDbtrAcct() : "---");
                row.createCell(4).setCellValue(tx.getDestinationCdtrAcct() != null ? tx.getDestinationCdtrAcct() : "---");

                // Injection numérique pour permettre les formules de Somme dans Excel
                Cell amountCell = row.createCell(5);
                amountCell.setCellValue(tx.getAmount().doubleValue());
                amountCell.setCellStyle(amountStyle);
            }

            // Ajustement automatique de la largeur des colonnes
            for (int i = 0; i < columns.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);
            return new ByteArrayInputStream(out.toByteArray());

        } catch (IOException e) {
            throw new RuntimeException("Erreur lors de la génération du fichier Excel", e);
        }
    }
}
