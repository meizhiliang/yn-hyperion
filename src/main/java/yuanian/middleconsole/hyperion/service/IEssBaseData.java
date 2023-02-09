package yuanian.middleconsole.hyperion.service;

import com.essbase.api.datasource.IEssCube;
import yuanian.middleconsole.hyperion.model.vo.DimData;

/**
 * @author meizhiliang
 * @projectName hyperion
 * @date 2022/10/25
 * @menu: TODO
 */
public interface IEssBaseData {
    /**
     * 通过报表查询语句查询多维库数据
     * @param cube
     * @param type
     * @param rep
     * @return DimData
     * @exception Exception
     */
    DimData executeReport(IEssCube cube, ReportType type, String rep)throws Exception;

    /**
     * 通过Mdx查询多维库数据
     * @param cube
     * @param mdxQuery
     * @return DimData
     * @exception Exception
     */
    DimData MdxQuery(IEssCube cube, String mdxQuery)throws Exception;

    /**
     * 将数据写入多维库
     * @param cube
     * @param data
     * @return
     * @exception
     */
    DimData loadData(IEssCube cube, String data)throws Exception;

    /**
     * 数据维度
     */
    public enum ReportType{
        scriptString,reportName,fileName
    }

    public enum DataType{
        String
    }

}
