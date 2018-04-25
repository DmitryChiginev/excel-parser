package com.dblm.excelparser.parse;

import com.dblm.excelparser.parse.parsePattern.*;
import com.dblm.excelparser.repository.ConnectionProperties;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.nio.file.Path;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Абстрактные методы парсера
 */
public class AbstractParser {

    private final static Logger log = LogManager.getLogger(AbstractParser.class);

    ParsePattern parsePattern;

    /**
     * Построение шаблона парсинга
     *
     * @param files
     * @return
     */
    public ParsePattern buldParsePattern(List<Path> files) {

        try {
            Path path = null;
            for (Path p : files) {
                if(p.toString().contains(ParsePattern.PARSE_PATTERN_FILE_NAME)) {
                    path = p;
                    break;
                }
            }
            if(path == null) {
                return null;
            }

            ParsePattern parsePattern = new ParsePattern();
            DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document document = documentBuilder.parse(path.toFile());
            Node root = document.getDocumentElement();

            NodeList childrenNodes = root.getChildNodes();
            for(int i = 0; i < childrenNodes.getLength(); i++) {
                Node nodeL1 = childrenNodes.item(i);
                if(nodeL1.getNodeName().trim().equalsIgnoreCase("db_dialect")) {
                    parsePattern.setDialectDB(ConnectionProperties.DialectDB.getInstance(nodeL1.getTextContent()));
                }
                if(nodeL1.getNodeName().trim().equalsIgnoreCase("db_url")) {
                    parsePattern.setDbURL(nodeL1.getTextContent());
                }
                if(nodeL1.getNodeName().trim().equalsIgnoreCase("db_username")) {
                    parsePattern.setUsername(nodeL1.getTextContent());
                }
                if(nodeL1.getNodeName().trim().equalsIgnoreCase("db_password")) {
                    parsePattern.setPassword(nodeL1.getTextContent());
                }
                if(nodeL1.getNodeName().trim().equalsIgnoreCase("db_schema")) {
                    parsePattern.setDbSchema(nodeL1.getTextContent());
                }
                if(nodeL1.getNodeName().trim().equalsIgnoreCase("db_table")) {
                    parsePattern.setDbTable(nodeL1.getTextContent());
                }
                if(nodeL1.getNodeName().trim().equalsIgnoreCase("db_operation")) {
                    parsePattern.setOperationDB(ConnectionProperties.OperationDB.getInstnce(nodeL1.getTextContent()));
                }
                if(nodeL1.getNodeName().trim().equalsIgnoreCase("data_rows_start_index")) {
                    parsePattern.setDataRowsStartIndex(nodeL1.getTextContent());
                }
                if(nodeL1.getNodeName().trim().equalsIgnoreCase("data_rows_end_index")) {
                    parsePattern.setDataRowsEndIndex(nodeL1.getTextContent());
                }
                if(nodeL1.getNodeName().trim().equalsIgnoreCase("search_data_start_index_with_addition")) {
                    parsePattern.setSearchDataRowsStartIndexWithAddition(nodeL1.getTextContent());
                }

                // Колонки
                if(nodeL1.getNodeName().trim().equalsIgnoreCase("column_mapping")) {
                    Set<Column> columnSet = new LinkedHashSet<>();
                    NodeList nodeL1ChildNodes = nodeL1.getChildNodes();
                    for(int j = 0; j < nodeL1ChildNodes.getLength(); j++) {
                        Node nodeL2 = nodeL1ChildNodes.item(j);
                        if(nodeL2.getNodeName().trim().equalsIgnoreCase("column")) {
                            Column column = this.buildColumnOrCell(nodeL2, new Column());
                            columnSet.add(column);
                        }
                    }
                    parsePattern.setColumnSet(columnSet);
                }

                // Отдельные ячейки
                if(nodeL1.getNodeName().trim().equalsIgnoreCase("cell_mapping")) {
                    Set<Cell> cellSet = new LinkedHashSet<>();
                    NodeList nodeL1ChildNodes = nodeL1.getChildNodes();
                    for(int j = 0; j < nodeL1ChildNodes.getLength(); j++) {
                        Node nodeL2 = nodeL1ChildNodes.item(j);
                        if(nodeL2.getNodeName().trim().equalsIgnoreCase("cell")) {
                            Cell cell = (Cell) this.buildColumnOrCell(nodeL2, new Cell());
                            cellSet.add(cell);
                        }
                    }
                    parsePattern.setCellSet(cellSet);
                }

                // Константные значения
                if(nodeL1.getNodeName().trim().equalsIgnoreCase("constant_mapping")) {
                    Set<Constanta> constantsSet = new LinkedHashSet<>();
                    NodeList nodeL1ChildNodes = nodeL1.getChildNodes();
                    for(int j = 0; j < nodeL1ChildNodes.getLength(); j++) {
                        Node nodeL2 = nodeL1ChildNodes.item(j);
                        if(nodeL2.getNodeName().trim().equalsIgnoreCase("constant")) {
                            NodeList nodeL2ChildNodes = nodeL2.getChildNodes();
                            Constanta constanta = new Constanta();
                            for(int k = 0; k < nodeL2ChildNodes.getLength(); k++) {
                                Node nodeL3 = nodeL2ChildNodes.item(k);
                                if(nodeL3.getNodeName().trim().equalsIgnoreCase("source")) {
                                    Source source = new Source();
                                    NamedNodeMap attributes = nodeL3.getAttributes();
                                    if(attributes.getNamedItem("type") != null) {
                                        source.setType(DataType.getInstence(attributes.getNamedItem("type").getTextContent()));
                                    }
                                    source.setValue(nodeL3.getTextContent());
                                    constanta.setSource(source);
                                }
                                if(nodeL3.getNodeName().trim().equalsIgnoreCase("target")) {
                                    Target target = new Target();
                                    target.setName(nodeL3.getTextContent());
                                    constanta.setTarget(target);
                                }
                            }
                            constantsSet.add(constanta);
                        }
                    }
                    parsePattern.setConstantsSet(constantsSet);
                }
            }
            this.parsePattern = parsePattern;
            return parsePattern;
        } catch (Exception e) {
            log.error("", e);
        }
        return null;
    }

    public Column buildColumnOrCell(Node nodeL2, Column column) {

        if(column == null || nodeL2 == null) {
            return null;
        }
        NodeList nodeL2ChildNodes = nodeL2.getChildNodes();
        for(int k = 0; k < nodeL2ChildNodes.getLength(); k++) {
            Node nodeL3 = nodeL2ChildNodes.item(k);
            if(nodeL3.getNodeName().trim().equalsIgnoreCase("source")) {
                Source source = new Source();
                NamedNodeMap attributes = nodeL3.getAttributes();
                if(attributes.getNamedItem("by") != null) {
                    source.setBy(Source.By.getInstance(attributes.getNamedItem("by").getTextContent()));
                }
                if(attributes.getNamedItem("type") != null) {
                    source.setType(DataType.getInstence(attributes.getNamedItem("type").getTextContent()));
                }
                if(attributes.getNamedItem("not-null") != null) {
                    source.setNotNull(attributes.getNamedItem("not-null").getTextContent().equalsIgnoreCase("true"));
                }
                if(attributes.getNamedItem("date-format") != null) {
                    source.setDateFormat(attributes.getNamedItem("date-format").getTextContent());
                }

                NodeList nodeL3ChildNodes = nodeL3.getChildNodes();
                for(int l = 0; l < nodeL3ChildNodes.getLength(); l++) {
                    Node nodeL4 = nodeL3ChildNodes.item(l);
                    if(nodeL4.getNodeName().trim().equalsIgnoreCase("name")) {
                        source.setName(nodeL4.getTextContent());
                    }
                    if(nodeL4.getNodeName().trim().equalsIgnoreCase("col_index")) {
                        source.setColIndex(nodeL4.getTextContent());
                    }
                    if(nodeL4.getNodeName().trim().equalsIgnoreCase("row_index")) {
                        source.setRowIndex(nodeL4.getTextContent());
                    }
                }
                column.setSource(source);
            }
            if(nodeL3.getNodeName().trim().equalsIgnoreCase("target")) {
                Target target = new Target();
                target.setName(nodeL3.getTextContent().trim());
                column.setTarget(target);
            }
            if(nodeL3.getNodeName().trim().equalsIgnoreCase("parse_strategy")) {
                ParseStrategy parseStrategy = new ParseStrategy();
                NodeList nodeL3ChildNodes = nodeL3.getChildNodes();
                for(int l = 0; l < nodeL3ChildNodes.getLength(); l++) {
                    Node nodeL4 = nodeL3ChildNodes.item(l);
                    if(nodeL4.getNodeName().trim().equalsIgnoreCase("start_index")) {
                        parseStrategy.setStartIndex(nodeL4.getTextContent());
                    }
                    if(nodeL4.getNodeName().trim().equalsIgnoreCase("end_index")) {
                        parseStrategy.setEndIndex(nodeL4.getTextContent());
                    }
                    if(nodeL4.getNodeName().trim().equalsIgnoreCase("regular_expression")) {
                        parseStrategy.setRegularExpression(nodeL4.getTextContent());
                    }
                }
                column.setParseStrategy(parseStrategy);
            }
            if(nodeL3.getNodeName().trim().equalsIgnoreCase("values")) {
                NodeList nodeL3ChildNodes = nodeL3.getChildNodes();
                Set<Value> valueSet = new LinkedHashSet<>();
                for(int l = 0; l < nodeL3ChildNodes.getLength(); l++) {
                    Node nodeL4 = nodeL3ChildNodes.item(l);
                    if(nodeL4.getNodeName().trim().equalsIgnoreCase("value")) {
                        Value v = new Value();
                        NodeList nodeL4ChildNodes = nodeL4.getChildNodes();
                        for(int m = 0; m < nodeL4ChildNodes.getLength(); m++) {
                            Node nodeL5 = nodeL4ChildNodes.item(m);
                            if(nodeL5.getNodeName().trim().equalsIgnoreCase("main")) {
                                v.setMain(nodeL5.getTextContent());
                            }
                            if(nodeL5.getNodeName().trim().equalsIgnoreCase("mean")) {
                                v.setMean(nodeL5.getTextContent());
                            }
                            if(nodeL5.getNodeName().trim().equalsIgnoreCase("case-sensitive")) {
                                v.setCaseInsensitive(nodeL5.getTextContent().equalsIgnoreCase("true"));
                            }
                            if(nodeL5.getNodeName().trim().equalsIgnoreCase("strict-equality")) {
                                v.setStrinctEquality(nodeL5.getTextContent().equalsIgnoreCase("true"));
                            }
                            if(nodeL5.getNodeName().trim().equalsIgnoreCase("alternative")) {
                                NodeList nodeL5ChildNodes = nodeL5.getChildNodes();
                                for(int n = 0; n < nodeL5ChildNodes.getLength(); n++) {
                                    Node nodeL6 = nodeL5ChildNodes.item(n);
                                    if(nodeL6.getNodeName().trim().equalsIgnoreCase("alt")) {
                                        v.addAlt(nodeL6.getTextContent());
                                    }
                                }
                            }
                        }
                        valueSet.add(v);
                    }
                }
                column.setValues(valueSet);
            }
            if(nodeL3.getNodeName().trim().equalsIgnoreCase("default_value")) {
                column.setDefaultValue(nodeL3.getTextContent());
            }
        }
        return column;
    }
}
