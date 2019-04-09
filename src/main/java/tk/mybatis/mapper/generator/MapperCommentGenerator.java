/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014-2016 abel533@gmail.com
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package tk.mybatis.mapper.generator;

import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;

import org.mybatis.generator.api.CommentGenerator;
import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.dom.java.CompilationUnit;
import org.mybatis.generator.api.dom.java.Field;
import org.mybatis.generator.api.dom.java.InnerClass;
import org.mybatis.generator.api.dom.java.InnerEnum;
import org.mybatis.generator.api.dom.java.JavaElement;
import org.mybatis.generator.api.dom.java.Method;
import org.mybatis.generator.api.dom.java.Parameter;
import org.mybatis.generator.api.dom.java.TopLevelClass;
import org.mybatis.generator.api.dom.xml.TextElement;
import org.mybatis.generator.api.dom.xml.XmlElement;
import org.mybatis.generator.config.MergeConstants;
import org.mybatis.generator.internal.util.StringUtility;

/**
 * if you use this jar , please build jackiehe2018/generator/generator-jackie and install to maven repository .
 * add <dependency>
 *          <groupId>tk.mybatis</groupId>
 *          <artifactId>mapper-spring-boot-starter</artifactId>
 *          <version>1.1.4</version>
 *      </dependency>
 * and  <plugin>
 *              <groupId>org.mybatis.generator</groupId>
 *              <artifactId>mybatis-generator-maven-plugin</artifactId>
 *              <version>1.3.5</version>
 *      </plugin>
 */

public class MapperCommentGenerator implements CommentGenerator {
    //开始的分隔符，例如mysql为`，sqlserver为[
    private String beginningDelimiter = "";
    //结束的分隔符，例如mysql为`，sqlserver为]
    private String endingDelimiter = "";
    //强制生成注解
    private boolean forceAnnotation;
    private Properties properties = new Properties();
    private boolean suppressDate = false;
    private boolean suppressAllComments = false;
    private SimpleDateFormat dateFormat;
    public MapperCommentGenerator() {
        super();
    }

    @Override
    public void addJavaFileComment(CompilationUnit compilationUnit) {
        return;
    }

    /**
     * xml中的注释
     *
     * @param xmlElement
     */
    @Override
    public void addComment(XmlElement xmlElement) {
        xmlElement.addElement(new TextElement("<!--"));
        StringBuilder sb = new StringBuilder();
        sb.append("  WARNING - ");
        sb.append(MergeConstants.NEW_ELEMENT_TAG);
        xmlElement.addElement(new TextElement(sb.toString()));
        xmlElement.addElement(new TextElement("-->"));
    }

    @Override
    public void addRootComment(XmlElement rootElement) {
        return;
    }

    @Override
    public void addConfigurationProperties(Properties properties) {
        String beginningDelimiter = properties.getProperty("beginningDelimiter");
        if (StringUtility.stringHasValue(beginningDelimiter)) {
            this.beginningDelimiter = beginningDelimiter;
        }
        String endingDelimiter = properties.getProperty("endingDelimiter");
        if (StringUtility.stringHasValue(endingDelimiter)) {
            this.endingDelimiter = endingDelimiter;
        }
        String forceAnnotation = properties.getProperty("forceAnnotation");
        if (StringUtility.stringHasValue(forceAnnotation)) {
            this.forceAnnotation = forceAnnotation.equalsIgnoreCase("TRUE");
        }
        this.properties.putAll(properties);
        this.suppressDate = StringUtility.isTrue(properties.getProperty("suppressDate"));
        this.suppressAllComments = StringUtility.isTrue(properties.getProperty("suppressAllComments"));
        String dateFormatString = properties.getProperty("dateFormat");
        if (StringUtility.stringHasValue(dateFormatString)) {
            this.dateFormat = new SimpleDateFormat(dateFormatString);
        }
    }

    public String getDelimiterName(String name) {
        StringBuilder nameBuilder = new StringBuilder();
        nameBuilder.append(beginningDelimiter);
        nameBuilder.append(name);
        nameBuilder.append(endingDelimiter);
        return nameBuilder.toString();
    }

    /**
     * 删除标记
     *
     * @param javaElement
     * @param markAsDoNotDelete
     */
    protected void addJavadocTag(JavaElement javaElement, boolean markAsDoNotDelete) {
        StringBuilder sb = new StringBuilder();
        sb.append(" * ");
        sb.append(MergeConstants.NEW_ELEMENT_TAG);
        if (markAsDoNotDelete) {
            sb.append(" do_not_delete_during_merge");
        }
        javaElement.addJavaDocLine(sb.toString());
    }

    /**
     * Example使用
     *
     * @param innerClass
     * @param introspectedTable
     */
    @Override
    public void addClassComment(InnerClass innerClass, IntrospectedTable introspectedTable) {
    }

    @Override
    public void addEnumComment(InnerEnum innerEnum, IntrospectedTable introspectedTable) {
    }

    /**
     * 给字段添加数据库备注
     *
     * @param field
     * @param introspectedTable
     * @param introspectedColumn
     */
    @Override
    public void addFieldComment(Field field, IntrospectedTable introspectedTable, IntrospectedColumn introspectedColumn) {
        if (StringUtility.stringHasValue(introspectedColumn.getRemarks())) {
            field.addJavaDocLine("/**");
            StringBuilder sb = new StringBuilder();
            sb.append(" * ");
            sb.append(introspectedColumn.getRemarks());
            field.addJavaDocLine(sb.toString());
            field.addJavaDocLine(" */");
            field.addJavaDocLine( "@ApiModelProperty(\""+ introspectedColumn.getRemarks() + "\")");
        }
        //添加注解
        if (field.isTransient()) {
            //@Column
            field.addAnnotation("@Transient");
        }
        for (IntrospectedColumn column : introspectedTable.getPrimaryKeyColumns()) {
            if (introspectedColumn == column) {
                field.addAnnotation("@Id");
                break;
            }
        }
        String column = introspectedColumn.getActualColumnName();
        if (StringUtility.stringContainsSpace(column) || introspectedTable.getTableConfiguration().isAllColumnDelimitingEnabled()) {
            column = introspectedColumn.getContext().getBeginningDelimiter()
                    + column
                    + introspectedColumn.getContext().getEndingDelimiter();
        }
        if (!column.equals(introspectedColumn.getJavaProperty())) {
            //@Column
            field.addAnnotation("@Column(name = \"" + getDelimiterName(column) + "\")");
        } else if (StringUtility.stringHasValue(beginningDelimiter) || StringUtility.stringHasValue(endingDelimiter)) {
            field.addAnnotation("@Column(name = \"" + getDelimiterName(column) + "\")");
        } else if(forceAnnotation){
            field.addAnnotation("@Column(name = \"" + getDelimiterName(column) + "\")");
        }
        if (introspectedColumn.isIdentity()) {
            if (introspectedTable.getTableConfiguration().getGeneratedKey().getRuntimeSqlStatement().equals("JDBC")) {
                field.addAnnotation("@GeneratedValue(generator = \"JDBC\")");
            } else {
                field.addAnnotation("@GeneratedValue(strategy = GenerationType.IDENTITY)");
            }
        } else if (introspectedColumn.isSequenceColumn()) {
            //在 Oracle 中，如果需要是 SEQ_TABLENAME，那么可以配置为 select SEQ_{1} from dual
            String tableName = introspectedTable.getFullyQualifiedTableNameAtRuntime();
            String sql = MessageFormat.format(introspectedTable.getTableConfiguration().getGeneratedKey().getRuntimeSqlStatement(), tableName, tableName.toUpperCase());
            field.addAnnotation("@GeneratedValue(strategy = GenerationType.IDENTITY, generator = \"" + sql + "\")");
        }
    }

    /**
     * 设置序列化ID的注释
     * @param field
     * @param introspectedTable
     */
    @Override
    public void addFieldComment(Field field, IntrospectedTable introspectedTable) {
        if (!this.suppressAllComments) {
            field.addJavaDocLine("/**");
            field.addJavaDocLine(" * Serializable ID");
            field.addJavaDocLine(" */");
        }
    }

    @Override
    public void addModelClassComment(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        if (!this.suppressAllComments) {
            topLevelClass.addJavaDocLine("import lombok.Data;");
            topLevelClass.addJavaDocLine("import lombok.NoArgsConstructor;");
            topLevelClass.addJavaDocLine("import io.swagger.annotations.ApiModel;");
            topLevelClass.addJavaDocLine("import io.swagger.annotations.ApiModelProperty;");
            topLevelClass.addJavaDocLine("import lombok.experimental.Accessors;\n");
            topLevelClass.addJavaDocLine("/**");
            String remarkLine = "";
            String remarks = introspectedTable.getRemarks();
            if (StringUtility.stringHasValue(remarks)) {
                String[] remarkLines = remarks.split(System.getProperty("line.separator"));
                String[] var5 = remarkLines;
                int var6 = remarkLines.length;
                for(int var7 = 0; var7 < var6; ++var7) {
                    remarkLine += var5[var7];
                }
            }
            StringBuilder sb = new StringBuilder();
            String name = String.valueOf(introspectedTable.getFullyQualifiedTable());
            sb.append(" * @className " + introspectedTable.getTableConfiguration().getDomainObjectName() + "\n");
            sb.append(" * @description " + remarkLine + " \n " + "* @author Jackie" + "\n");
            sb.append(" * @date " + new SimpleDateFormat("yyyy年MM月dd日 HH:mm:ss").format(new Date()) + "\n");
            sb.append(" * @version 1.0");
            topLevelClass.addJavaDocLine(sb.toString());
            this.addJavadocTag(topLevelClass, true);
            topLevelClass.addJavaDocLine(" */\n");
            topLevelClass.addJavaDocLine("@Data");
            topLevelClass.addJavaDocLine("@NoArgsConstructor");
            topLevelClass.addJavaDocLine("@Accessors(chain=true)");
            topLevelClass.addJavaDocLine("@ApiModel(\""+ remarkLine +"\")");
        }
    }

    /**
     * @param method
     * @param introspectedTable
     */
    @Override
    public void addGeneralMethodComment(Method method, IntrospectedTable introspectedTable) {
    }

    /**
     * getter方法注释
     *
     * @param method
     * @param introspectedTable
     * @param introspectedColumn
     */
    @Override
    public void addGetterComment(Method method, IntrospectedTable introspectedTable, IntrospectedColumn introspectedColumn) {
        StringBuilder sb = new StringBuilder();
        method.addJavaDocLine("/**");
        if (StringUtility.stringHasValue(introspectedColumn.getRemarks())) {
            sb.append(" * 获取");
            sb.append(introspectedColumn.getRemarks());
            method.addJavaDocLine(sb.toString());
            method.addJavaDocLine(" *");
        }
        sb.setLength(0);
        sb.append(" * @return ");
        sb.append(introspectedColumn.getActualColumnName());
        if (StringUtility.stringHasValue(introspectedColumn.getRemarks())) {
            sb.append(" - ");
            sb.append(introspectedColumn.getRemarks());
        }
        method.addJavaDocLine(sb.toString());
        method.addJavaDocLine(" */");
    }

    /**
     * setter方法注释
     *
     * @param method
     * @param introspectedTable
     * @param introspectedColumn
     */
    @Override
    public void addSetterComment(Method method, IntrospectedTable introspectedTable, IntrospectedColumn introspectedColumn) {
        StringBuilder sb = new StringBuilder();
        method.addJavaDocLine("/**");
        if (StringUtility.stringHasValue(introspectedColumn.getRemarks())) {
            sb.append(" * 设置");
            sb.append(introspectedColumn.getRemarks());
            method.addJavaDocLine(sb.toString());
            method.addJavaDocLine(" *");
        }
        Parameter parm = method.getParameters().get(0);
        sb.setLength(0);
        sb.append(" * @param ");
        sb.append(parm.getName());
        if (StringUtility.stringHasValue(introspectedColumn.getRemarks())) {
            sb.append(" ");
            sb.append(introspectedColumn.getRemarks());
        }
        method.addJavaDocLine(sb.toString());
        method.addJavaDocLine(" */");
    }

    /**
     * Example使用
     *
     * @param innerClass
     * @param introspectedTable
     * @param markAsDoNotDelete
     */
    @Override
    public void addClassComment(InnerClass innerClass, IntrospectedTable introspectedTable, boolean markAsDoNotDelete) {
    }
}
