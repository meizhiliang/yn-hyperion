package yuanian.middleconsole.hyperion.service.impl;

import com.essbase.api.base.EssException;
import com.essbase.api.base.IEssValueAny;
import com.essbase.api.dataquery.*;
import com.essbase.api.datasource.IEssCube;
import org.springframework.stereotype.Service;
import yuanian.middleconsole.hyperion.model.vo.DimData;
import yuanian.middleconsole.hyperion.service.IEssBaseData;

/**
 * @author meizhiliang
 * @projectName hyperion
 * @date 2022/10/25
 * @menu: TODO
 */
@Service
public class IEssBaseDataImpl implements IEssBaseData {

    @Override
    public DimData executeReport(IEssCube cube, ReportType type, String rep) throws Exception{
        String out = null;
        switch(type){
            case scriptString:
                out = cube.report(rep, true, false);
                break;
            case reportName:
                out =  cube.report(true, false, rep, false);
                break;
            case fileName:
                out =  cube.report(true, false, rep, true);
                break;
        }
        return new DimData(DimData.SUCCESSFUL,out);
    }

    @Override
    public DimData MdxQuery(IEssCube cube,String mdxQuery)  throws Exception{
        boolean bDataLess = false;
        boolean bNeedCellAttributes = false;
        boolean bHideData = true;
        IEssCubeView cv = cube.openCubeView("Mdx Query");
        IEssOpMdxQuery op = cv.createIEssOpMdxQuery();
        op.setQuery(bDataLess, bHideData, mdxQuery, bNeedCellAttributes,IEssOpMdxQuery.EEssMemberIdentifierType.NAME);
        op.setXMLAMode(false);
        op.setNeedFormattedCellValue(true);
        op.setNeedSmartlistName(true);
        op.setNeedFormatString(false);
        op.setNeedMeaninglessCells(false);
        cv.performOperation(op);
        IEssMdDataSet mddata = cv.getMdDataSet();
        StringBuffer buf = new StringBuffer();
        printMdDataSetInGridForm(mddata,buf);
        return new DimData(DimData.SUCCESSFUL,buf.toString());
    }

    @Override
    public DimData loadData(IEssCube cube,String data) throws Exception{
        cube.beginUpdate(true, false);
        cube.sendString(data);
        cube.endUpdate();
        return new DimData(DimData.SUCCESSFUL,null);
    }


    private static void printMdDataSetInGridForm(IEssMdDataSet mddata, StringBuffer buf) throws Exception {
        IEssMdAxis[] axes = mddata.getAllAxes();
        IEssMdAxis slicerOrPovAxis = null;
        IEssMdAxis columnAxis = null;
        IEssMdAxis rowAxis = null;
        IEssMdAxis pageAxis = null;
        // If slicer axis is present then it will be 1st axis.
        int axisIndex = 0;
        if (axes[axisIndex].isSlicerAxis()) {
            slicerOrPovAxis = axes[axisIndex++];
        }
        columnAxis = axes[axisIndex++];
        // row axis is present.
        if (axisIndex < axes.length) {
            rowAxis = axes[axisIndex++];
        }
        //page axis is present.
        if (axisIndex < axes.length) {
            pageAxis = axes[axisIndex];
        }
        if (slicerOrPovAxis != null) {
            IEssMdMember[] mem = slicerOrPovAxis.getAllTupleMembers(0);
            String sTuple = "(";
            int i = 0;
            for (i = 0; i < mem.length - 1; i++) {
                sTuple += "[" + mem[i].getName() + "], ";
            }
            sTuple += "[" + mem[i].getName() + "])";

            buf.append("Slicer(POV) Tuple: ").append(sTuple).append("\n");
        }


        if (pageAxis != null) {
            for (int i = 0; i < pageAxis.getTupleCount(); i++) {
                // Print Page Header.
                String pageHeader = "------- PAGE " + i + ", Tuple: (";
                String pageFooter = "------- PAGE " + i + " ends -------";
                IEssMdMember[] mem = pageAxis.getAllTupleMembers(i);
                int j;
                for (j = 0; j < mem.length - 1; j++) {
                    pageHeader += "[" + mem[j].getName() + "], ";
                }
                pageHeader += "[" + mem[j].getName() + "]) --------";

                System.out.println(pageHeader);
                printMdDataSetInGridForm_Rows_Columns(mddata, columnAxis,
                        rowAxis, i, buf);
                System.out.println(pageFooter);
            }
        } else if (rowAxis != null) {
            printMdDataSetInGridForm_Rows_Columns(mddata, columnAxis, rowAxis, 0, buf);
        }
    }

    private static void printMdDataSetInGridForm_Rows_Columns(
            IEssMdDataSet mddata, IEssMdAxis columnAxis, IEssMdAxis rowAxis,
            int pageNum, StringBuffer buf) throws EssException {
        int cols = columnAxis.getTupleCount();
        int rows = rowAxis.getTupleCount();
        if (cols <= 0 || rows <= 0) {
            System.out.println("This Sample has limited support for printing this MDX result in Grid form " +
                    "because this has "+cols+" columns & "+rows+" rows.");
            System.out.println("Note: If there no rows and 'n' number of columns, you can still print result in table format by modifying this sample code.");
            return;
        }
        IEssMdMember[] mem = rowAxis.getAllTupleMembers(0);
        printRowTitle(mem,buf);
        for (int j = 0; j < cols; j++) {
            mem = columnAxis.getAllTupleMembers(j);
            buf.append("\t");
            printTuple(mem,buf);
        }

        int k = pageNum * cols * rows;
        for (int i = 0; i < rows; i++) {
            buf.append("\n");
            mem = rowAxis.getAllTupleMembers(i);
            printTuple(mem,buf);

            buf.append("\t");
            for (int l = 0; l < mem.length; l++) {
                int propcnt = mem[l].getCountProperties();
                for (int propInd = 0; propInd < propcnt; propInd++) {
                    IEssValueAny propval = mem[l].getPropertyValueAny(propInd);
                    buf.append(mem[l].getPropertyName(propInd) + ", "
                            + propval );
                }
            }
            for (int j = 0; j < cols; j++) {
                if (mddata.isMissingCell(k)) {
                    buf.append("\t#Missing");
                } else if (mddata.isNoAccessCell(k)) {
                    buf.append("\t#NoAccess");
                } else {
                    String fmtdCellTxtVal = mddata.getFormattedValue(k);
                    if (fmtdCellTxtVal != null && fmtdCellTxtVal.length() > 0) {
                        buf.append("\t").append(fmtdCellTxtVal);
                    } else {
                        double val = mddata.getCellValue(k);
                        buf.append("\t").append(val);
                    }
                }
                k++;
            }
        }
    }
    private static void printTuple(IEssMdMember[] mem, StringBuffer buf) throws EssException {
        buf.append("(");
        for (int i = 0; i < mem.length - 1; i++){
            buf.append(mem[i].getName()).append(", ");
        }
        buf.append(mem[mem.length - 1].getName()).append(")");
    }

    private static void printRowTitle(IEssMdMember[] mem, StringBuffer buf) throws EssException {
        buf.append("(");
        for (int i = 0; i < mem.length - 1; i++){
            buf.append(mem[i].getDimension().getName()).append(", ");
        }
        buf.append(mem[mem.length - 1].getDimension().getName()).append(")");
    }

}
