import jsPDF from 'jspdf';
import autoTable from 'jspdf-autotable';

export const generatePDFReport = (casos, groups, memberNames = []) => {
  const doc = new jsPDF();
  
  // Headers
  doc.setFontSize(22);
  doc.setTextColor(0, 51, 102); // Dark blue
  doc.text("Informe de Simulacion", 14, 22);
  
  doc.setFontSize(14);
  doc.setTextColor(100);
  doc.text("Fiscalia General de la Nacion - Tablero Analitico", 14, 32);

  // Group Members
  let currentY = 45;
  if (memberNames.length > 0) {
    doc.setFontSize(12);
    doc.setTextColor(0, 51, 102);
    doc.text("Integrantes del Grupo:", 14, currentY);
    currentY += 7;
    doc.setTextColor(50);
    memberNames.forEach(name => {
      if (name.trim()) {
        doc.text(`- ${name.trim()}`, 18, currentY);
        currentY += 6;
      }
    });
    currentY += 5;
  }
  
  // General Stats
  const totalCases = casos.length;
  const groupedCases = new Set(groups.flatMap(g => g.cases)).size;
  const ungroupedCases = totalCases - groupedCases;
  
  doc.setFontSize(12);
  doc.setTextColor(0);
  doc.text(`Total de casos analizados: ${totalCases}`, 14, currentY);
  currentY += 7;
  doc.text(`Casos agrupados: ${groupedCases}`, 14, currentY);
  currentY += 7;
  doc.text(`Casos sin grupo: ${ungroupedCases}`, 14, currentY);
  currentY += 7;
  doc.text(`Grupos creados: ${groups.length}`, 14, currentY);
  currentY += 10;
  
  // Group Details
  if (groups.length === 0) {
    doc.setFontSize(12);
    doc.text("No se crearon grupos durante esta simulacion.", 14, currentY);
  } else {
    groups.forEach((group, index) => {
      // Group Title
      doc.setFontSize(16);
      doc.setTextColor(0, 51, 102);
      doc.text(`Grupo ${index + 1}: ${group.name}`, 14, currentY);
      currentY += 8;
      
      doc.setFontSize(10);
      doc.setTextColor(50);
      doc.text(`Asociado por: ${group.type}`, 14, currentY);
      currentY += 6;
      
      const justifText = doc.splitTextToSize(`Justificacion de creacion: ${group.justification}`, 180);
      doc.text(justifText, 14, currentY);
      currentY += justifText.length * 5 + 4;
      
      // Cases Table
      const casesData = group.cases.map(c => {
        // Parse the delito directly from the filename (which is the case name)
        const parts = c.split('_');
        let delito = '';
        let radicado = c;
        
        if (parts.length > 1) {
          radicado = parts[0];
          delito = parts[1].split('.')[0].trim().toUpperCase();
        } else if (c.includes('.')) {
          radicado = c.split('.')[0];
        }

        return [radicado, delito];
      });
      
      autoTable(doc, {
        startY: currentY,
        head: [['Radicado', 'Delito']],
        body: casesData,
        theme: 'grid',
        headStyles: { fillColor: [30, 58, 138] }, // #1E3A8A
        margin: { left: 14 }
      });
      
      currentY = doc.lastAutoTable.finalY + 10;
      
      // Decisions Section
      if (group.decision) {
        doc.setFontSize(12);
        doc.setTextColor(0, 100, 0); // Green
        doc.text("Decisiones Tomadas:", 14, currentY);
        currentY += 8;
        
        const decisionEntries = Object.entries(group.decision.options)
          .filter(([_, isChecked]) => isChecked)
          .map(([option]) => option);
          
        if (decisionEntries.length > 0) {
          decisionEntries.forEach(option => {
            // Check page break before decision block
            if (currentY > 260) {
              doc.addPage();
              currentY = 20;
            }
            
            doc.setFontSize(11);
            doc.setTextColor(0, 51, 102); // Dark blue for decision title
            doc.text(`- ${option}`, 14, currentY);
            currentY += 6;
            
            doc.setFontSize(10);
            doc.setTextColor(50); // Dark gray for text
            const just = group.decision.justifications[option] || "Sin justificacion";
            const justLines = doc.splitTextToSize(just, 180);
            doc.text(justLines, 18, currentY);
            currentY += justLines.length * 5 + 4;
          });
          currentY += 5; // Extra spacing after decisions
        } else {
          doc.setFontSize(10);
          doc.setTextColor(100);
          doc.text("Se marco una decision pero sin opciones seleccionadas.", 14, currentY);
          currentY += 10;
        }
      } else {
        doc.setFontSize(10);
        doc.setTextColor(200, 0, 0); // Red
        doc.text("No se tomo ninguna decision para este grupo.", 14, currentY);
        currentY += 15;
      }
      
      // Page break logic handled manually for groups now
      if (currentY > 260) {
        doc.addPage();
        currentY = 20;
      }
    });
  }
  
  doc.save('Informe_Simulacion_Nexus.pdf');
};
