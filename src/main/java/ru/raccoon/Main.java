package ru.raccoon;

import com.google.gson.reflect.TypeToken;
import com.opencsv.*;
import com.opencsv.bean.ColumnPositionMappingStrategy;
import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.*;
import org.w3c.dom.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

public class Main {
    public static void main(String[] args) throws Exception {

        String[] columnMapping = {"id", "firstName", "lastName", "country", "age"};

        String fileName = "data.csv";

        List<Employee> list = parseCSV(columnMapping, fileName);

        String json = listToJson(list);

        writeString(json, "data.json");

        String json2 = listToJson(parseXML());

        writeString(json2, "data2.json");

    }

    //метод парсинга csv в список сотрудников
    private static List<Employee> parseCSV(String[] columnMapping, String fileName) {

        try (CSVReader reader = new CSVReader(new FileReader(fileName))) {

            ColumnPositionMappingStrategy<Employee> strategy = new ColumnPositionMappingStrategy<>();
            strategy.setType(Employee.class);
            strategy.setColumnMapping(columnMapping);

            CsvToBean<Employee> csv = new CsvToBeanBuilder<Employee>(reader)
                    .withMappingStrategy(strategy)
                    .build();

            return csv.parse();

        } catch (IOException e) {
            System.out.println(e.getMessage());
        }

        return null;
    }

    private static List<Employee> parseXML() throws Exception {

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(new File("data.xml"));

        Node root = doc.getDocumentElement();
        return readEmployees(root);
    }

    private static List<Employee> readEmployees(Node node) {

        List<Employee> employees = new ArrayList<>(); //подготовим список для сотрудников
        NodeList nodeList = node.getChildNodes();
        //работаем с дочерними нодами
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node node_ = nodeList.item(i);
            //если дочерняя нода элемент и сотрудник
            if ((Node.ELEMENT_NODE == node_.getNodeType()) && ("employee".equals(node_.getNodeName()))) {
                //то кастим ноду в элемент
                Element nNode = (Element) node_;
                //подготавливаем переменные для входных атрибутов конструктора сотрудника
                long id = Long.parseLong(nNode.getElementsByTagName("id").item(0).getTextContent());
                String firstName = nNode.getElementsByTagName("firstName").item(0).getTextContent();
                String lastName = nNode.getElementsByTagName("lastName").item(0).getTextContent();
                String country = nNode.getElementsByTagName("country").item(0).getTextContent();
                int age = Integer.parseInt(nNode.getElementsByTagName("age").item(0).getTextContent());
                //добавляем в список нового сотрудника
                employees.add(new Employee(id, firstName, lastName, country, age));
                }
                //рекурсивно запускаем проверку дочерних нод текущего узла
                readEmployees(node_);
            }
        return employees;
        }

    //перевод списка сотрудников в строку в JSON-формате
    private static String listToJson(List<Employee> list) {
        GsonBuilder builder = new GsonBuilder();
        builder.setPrettyPrinting();
        Gson gson = builder.create();

        Type listType = new TypeToken<List<Employee>>() {
        }.getType();

        return gson.toJson(list, listType);
    }

    //запись строки в файл
    private static void writeString(String str, String fileName) {

        try (FileWriter fileWriter = new FileWriter(fileName)) {
            fileWriter.write(str);
            fileWriter.flush();
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }
}