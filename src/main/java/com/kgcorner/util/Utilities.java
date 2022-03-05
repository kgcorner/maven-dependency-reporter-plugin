package com.kgcorner.util;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.kgcorner.models.D3CompatibleNodes;
import com.kgcorner.models.DependencyArtifact;
import com.kgcorner.models.DependencyDetails;
import com.kgcorner.models.Link;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Description : Utilities classes. used for writing report
 * Author: kumar
 * Created on : 03/03/22
 */

public class Utilities {
    private static final String D3HTML_TEMPLATE="<!DOCTYPE html><meta charset=\"utf-8\"><link href=\"https://cdn.jsdelivr.net/npm/bootstrap@5.1.3/dist/css/bootstrap.min.css\" rel=\"stylesheet\" integrity=\"sha384-1BmE4kWBq78iYhFldvKuhfTAU6auU8tT94WrHftjDbrCEXSU1oBoqyl2QvZ6jIW3\" crossorigin=\"anonymous\"><style>.link{stroke:#ccc}.node text{pointer-events:none;font:10px sans-serif}td{margin:10px;border:thin ridge maroon;padding:15px}table{width:100%}.node{cursor:pointer}.filter-container{display:block;width:80%;margin:40px auto}.filter-text{width:80%;height:65px;padding:15px;line-height:65px;margin-right:2%}.filter-button{background:#387413;padding:10px;color:#fff;border-radius:5px;height:65px;width:150px}</style><body><h1 style=\"text-align:center;margin-top:20px\"> Dependency Report : **projectName** </h1><div class=\"filter-container\"><input type=\"text\" name=\"dependency\" placeholder=\"Enter dependency\" id=\"filterDep\" class=\"filter-text\"/><input type=\"submit\" class=\"filter-button\" onclick=\"filter()\" /></div><div class=\"row\"><div class=\"col-lg-8\"><div class=\"network\"></div></div><div class=\"col-lg-4\"><table><tr><td>Project Name</td><td><label id=\"prjName\" value=\"Project\"> NA </label></td></tr><tr><td>Project Version</td><td><label id=\"prjVersion\" value=\"Project\"> NA </label></td></tr><tr><td>Project Group Id</td><td><label id=\"prjGrpId\" value=\"Project\"> NA </label></td></tr><tr><td>Total dependencies</td><td><label id=\"prjDepCount\" value=\"Project\"> NA </label></td></tr><tr><td>Dependent Project Count</td><td><label id=\"prjDependentCount\" value=\"Project\"> NA </label></td></tr><tr><td>Dependent Projects</td><td><label id=\"prjDep\" value=\"Project\"> NA </label></td></tr></table></div><footer style=\"position:relative;bottom:0;height:30px;border-top:thin ridge maroon;text-align:right;margin-right:20px\"><span> Generated by <a href=\"https://github.com/kgcorner/maven-dependency-reporter-plugin\">Maven Dependency Reporter Plugin</a></span></footer></div><script src=\"https://d3js.org/d3.v3.min.js\"></script><script>let data=**Data**;var width=1000,height=924;function drawNetwork(e){var r=d3.select('.network').append('svg').attr('width',width).attr('height',height),n=d3.layout.force().gravity(0.05).distance(100).charge(-30).size([width,height]);n.nodes(e.nodes).links(e.links).start();var a=r.selectAll('.link').data(e.links).enter().append('line').attr('class','link'),t=r.selectAll('.node').data(e.nodes).enter().append('g').attr('class','node').call(n.drag);t.append('image').attr('xlink:href','https://iconarchive.com/download/i103574/custom-icon-design/flatastic-6/Circle.ico').attr('x',-8).attr('y',-8).attr('width',16).attr('height',16);t.append('text').attr('dx',12).attr('dy','.35em').text(function(e){return e.project});n.on('tick',function(){a.attr('x1',function(e){return e.source.x}).attr('y1',function(e){return e.source.y}).attr('x2',function(e){return e.target.x}).attr('y2',function(e){return e.target.y});t.attr('transform',function(e){return'translate('+e.x+','+e.y+')'});t.on('click',(d)=>{var r=document.getElementById('prjName'),i=document.getElementById('prjVersion'),o=document.getElementById('prjDepCount'),c=document.getElementById('prjDependentCount'),l=document.getElementById('prjDep'),p=document.getElementById('prjGrpId');r.innerHTML=d.project;i.innerHTML=d.projectVersion;o.innerHTML=d.dependencies.length;c.innerHTML=d.dependents.length;p.innerHTML=d.projectGroupId;var e='<ul>';d.dependents.forEach(dep=>{var n='<li>'+dep.dependencyGroupId+':'+dep.dependency+':'+dep.dependencyVersion+'</li>';e=e+n});e=e+'</ul>';l.innerHTML=e})})};drawNetwork(data);function filter(){var n=document.getElementById('filterDep'),t=n.value,e=[];let newData={};data.nodes.forEach(node=>{node.dependencies.forEach(dep=>{if(dep.dependency.indexOf(t)>-1){e.push(node)}else{if(dep.dependencyGroupId.indexOf(t)>-1){e.push(node)}}})});newData.nodes=e;let newLinks=[];for(let i=0;i<newData.nodes.length;i++){let node=newData.nodes[i];node.dependencies.forEach(dep=>{let index=getIndex(newData.nodes,dep.dependency);if(index>-1){let link={source:index,target:i};newLinks.push(link)}})};newData.links=newLinks;console.log(newData);d3.select('svg').remove();drawNetwork(newData)};function getIndex(e,t){for(let i=0;i<e.length;i++){if(e[i].project.indexOf(t)>-1)return i};return-1};</script></body>";
    private static void writeCsv(List<DependencyArtifact> data, String path) throws IOException {
        String pattern = "\"%s\",\"%s\",\"%s\",\"%s\"\n";
        String header = "Project, Project Version, Dependency, Dependency version\n";
        FileOutputStream fileOutputStream = new FileOutputStream(new File(path));
        fileOutputStream.write(header.getBytes());
        for(DependencyArtifact record : data) {
            String entry = String.format(pattern, record.getProject(), record.getProjectVersion(),
                record.getDependency(), record.getDependencyVersion());
            fileOutputStream.write(entry.getBytes());
        }
        fileOutputStream.close();
    }

    public static void write(List<DependencyArtifact> dependencies, String outputFile, String format, String projectName) throws IOException {
        if(format.equalsIgnoreCase("csv")) {
            writeCsv(dependencies, outputFile);
        }
        if(format.equalsIgnoreCase("json")) {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            String jsonData = gson.toJson(dependencies);
            FileOutputStream fileOutputStream = new FileOutputStream(new File(outputFile));
            fileOutputStream.write(jsonData.getBytes());
        }
        if(format.equalsIgnoreCase("html"))
            writeD3CompatibleData(dependencies, outputFile, projectName);
    }

    private static void writeD3CompatibleData(List<DependencyArtifact> dependencies, String outputFile, String projectName) throws IOException {
        D3CompatibleNodes d3CompatibleNodes = new D3CompatibleNodes();
        d3CompatibleNodes.setNodes(dependencies);
        d3CompatibleNodes.setLinks(new ArrayList<>());
        List<Link> links = new ArrayList<>();
        for (int i = 0; i < dependencies.size(); i++) {
            DependencyArtifact artifact = dependencies.get(i);
            List<DependencyDetails> dependenciesList = artifact.getDependencies();
            for(DependencyDetails details : dependenciesList) {
                int index = getIndexOfProject(dependencies, details.getDependency());
                if(index != -1) {
                    Link link = new Link();
                    link.setSource(index);
                    link.setTarget(i);
                    d3CompatibleNodes.getLinks().add(link);
                }

            }
        }
        //String htmlFile = Utilities.class.getResource("/index.html").getPath();
        String htmlData = D3HTML_TEMPLATE;

        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String jsonData = gson.toJson(d3CompatibleNodes);
        htmlData = htmlData.replace("**projectName**", projectName);
        htmlData = htmlData.replace("**Data**", jsonData);
        FileOutputStream fileOutputStream = new FileOutputStream(new File(outputFile));
        fileOutputStream.write(htmlData.getBytes());
    }

    private static int getIndexOfProject(List<DependencyArtifact> dependencies, String projectName) {
        for (int i = 0; i < dependencies.size(); i++) {
            if(dependencies.get(i).getProject().equals(projectName))
                return i;
        }
        return -1;
    }

    private static String readFile(String fileAddress) throws IOException {
        File file = new File(fileAddress);
        BufferedReader br
            = new BufferedReader(new FileReader(file));

        StringBuffer sb = new StringBuffer();
        String st;
        while ((st = br.readLine()) != null) {
            sb.append(st);
        }
        return sb.toString();
    }
}
