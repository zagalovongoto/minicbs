package org.gimuemoa.minicbs.service.impl;

import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import lombok.RequiredArgsConstructor;
import org.gimuemoa.minicbs.dto.BankTransactionDTO;
import org.gimuemoa.minicbs.service.PdfGeneratorService;
import org.gimuemoa.minicbs.service.SystemParameterService;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
public class PdfGeneratorServiceImpl implements PdfGeneratorService {

    private final SystemParameterService paramService;

    @Override
    public ByteArrayInputStream generateTransactionReceipt(BankTransactionDTO tx) {
        // Lecture dynamique du nom de la banque depuis la table system_parameters
        String bankName = paramService.getRequiredString("BANK_DISPLAY_NAME");

        Document document = new Document(PageSize.A5); // Format ticket A5 compact
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try {
            PdfWriter.getInstance(document, out);
            document.open();

            // 1. EN-TÊTE DU TICKET DE COMPENSATION
            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16, new Color(5, 60, 120));
            Paragraph title = new Paragraph(bankName.toUpperCase(), titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);

            Font subTitleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8, Color.GRAY);
            Paragraph subTitle = new Paragraph("REÇU OFFICIEL D'OPÉRATION DE GUICHET", subTitleFont);
            subTitle.setAlignment(Element.ALIGN_CENTER);
            subTitle.setSpacingAfter(25);
            document.add(subTitle);

            // 2. TABLEAU DES ÉCRITURES FINANCIÈRES (2 colonnes)
            PdfPTable table = new PdfPTable(2);
            table.setWidthPercentage(100);
            table.setWidths(new int[]{4, 6});

            Font labelFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, Color.DARK_GRAY);
            Font valueFont = FontFactory.getFont(FontFactory.HELVETICA, 10, Color.BLACK);
            Font fontMonospace = FontFactory.getFont(FontFactory.COURIER, 10, Color.BLACK);

            addTableCell(table, "Référence GIM :", tx.getReference(), labelFont, fontMonospace);
            addTableCell(table, "Nature Opération :", tx.getType(), labelFont, valueFont);

            if (tx.getExecutedAt() != null) {
                addTableCell(table, "Date d'exécution :", tx.getExecutedAt().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")), labelFont, fontMonospace);
            }

            if (tx.getSourceDbtrAcct() != null) {
                addTableCell(table, "Compte Débiteur :", tx.getSourceDbtrAcct(), labelFont, fontMonospace);
            }
            if (tx.getDestinationCdtrAcct() != null) {
                addTableCell(table, "Compte Créditeur :", tx.getDestinationCdtrAcct(), labelFont, fontMonospace);
            }

            // Ligne de séparation
            PdfPCell hrLabel = new PdfPCell(new Phrase(""));
            hrLabel.setBorder(Rectangle.BOTTOM);
            hrLabel.setBorderColor(Color.LIGHT_GRAY);
            PdfPCell hrValue = new PdfPCell(new Phrase(""));
            hrValue.setBorder(Rectangle.BOTTOM);
            hrValue.setBorderColor(Color.LIGHT_GRAY);
            table.addCell(hrLabel);
            table.addCell(hrValue);

            // Affichage mis en valeur du Montant Net comptabilisé
            Font amountFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, new Color(22, 163, 74));
            addTableCell(table, "Montant Net Mouvementé :", String.format("%,.0f", tx.getAmount()) + " XOF", labelFont, amountFont);

            document.add(table);

            // 3. PIED DE PAGE & SÉCURITÉ REGLÉMENTAIRE
            Paragraph details = new Paragraph("\nDescription : " + tx.getDescription(), FontFactory.getFont(FontFactory.HELVETICA, 9, Color.DARK_GRAY));
            details.setAlignment(Element.ALIGN_LEFT);
            document.add(details);

            Paragraph footer = new Paragraph("\n\n\nCe document fait foi de reçu numérique immuable pour le compte de règlement de la Banque Centrale (BCEAO). Généré par le système Mini CBS.", FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 7, Color.LIGHT_GRAY));
            footer.setAlignment(Element.ALIGN_CENTER);
            document.add(footer);

            document.close();
        } catch (DocumentException ex) {
            ex.printStackTrace();
        }

        return new ByteArrayInputStream(out.toByteArray());
    }

    private void addTableCell(PdfPTable table, String label, String value, Font labelFont, Font valueFont) {
        PdfPCell cellLabel = new PdfPCell(new Phrase(label, labelFont));
        cellLabel.setBorder(Rectangle.NO_BORDER);
        cellLabel.setPadding(6);

        PdfPCell cellValue = new PdfPCell(new Phrase(value, valueFont));
        cellValue.setBorder(Rectangle.NO_BORDER);
        cellValue.setPadding(6);
        cellValue.setHorizontalAlignment(Element.ALIGN_RIGHT);

        table.addCell(cellLabel);
        table.addCell(cellValue);
    }
}
