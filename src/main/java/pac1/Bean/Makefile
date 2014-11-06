#
# Makefile for Bean 
#

EXPORT = ../../../export/gui/netstorm/WEB-INF/lib
EXPORT_ROOT = ../../../export/gui/ROOT/WEB-INF/lib

OBJ = ../../../obj/gui/bean

OBJ_PKG = $(OBJ)/pac1/Bean

JAR_OBJ = ../../../obj/gui/netstorm/lib

MON_OBJ = ../../../obj/monitors/lib

GetOptJar=../../../thirdparty/jar/java-getopt-1.0.9.jar

RUNlOGIC_SRC = ../RunLogic/

SERVLET_CLIENT_SRC = ../servlet_client/

JAVA_FLAGS = 
JAVAC= javac

export CLASSPATH=../jar/poi.jar:../jar/servlet-api.jar:../jar/jcommon-1.0.17.jar:../jar/itext-paulo-154.jar:$(GetOptJar):../advisory/java::../jar/spring-2.5.6.SEC01.jar:../jar/esper-4.0.0.jar:../jar/commons-logging-1.1.1.jar:../jar/cglib-nodep-2.2.jar:../jar/antlr-runtime-3.2.jar:../jar/log4j-1.2.16.jar:java/com/netstorm/advice/event/:$(SERVLET_CLIENT_SRC):../jar/jnlp.jar:../jar/servlet-api.jar:../jar/asm-all-4.0.jar:$(RUNlOGIC_SRC):./VUserTrace/:./nfLayouts/:../jar/signed_jars/harlib-1.0b10.jar:../jar/signed_jars/harlib-jackson-1.0b10.jar:../jar/jfreechart-1.0.14.jar:../jar/signed_jars/jmimemagic-0.1.0.jar:../jar/signed_jars/apache-jakarta-oro.jar:$(MON_OBJ)/CmonLib.jar:../jar/signed_jars/json_simple-1.1.jar:../jar/signed_jars/evalutor.jar:../jar/signed_jars/mail-1.4.jar:../jar/signed_jars/javaee-api-5.0.3.jar:../jar/signed_jars/commons-lang-2.6.jar:../jar/signed_jars/commons-io-2.2.jar:../jar/signed_jars/swingx-all-1.6.4.jar:.
all: build release

build: FileBean.class

release: 
	#DO not remove export dir as this has jars exported by jar makefile
	rm -rf $(EXPORT)/netstorm_bean.jar
	
	mkdir -p $(EXPORT)
	mkdir -p $(JAR_OBJ) $(EXPORT_ROOT)
	cp $(OBJ)/netstorm_bean.jar $(EXPORT)/
	cp $(MON_OBJ)/CmonLib.jar $(EXPORT)/ # this is to copy Cmonlib.jar for browsing feature.
	cp $(OBJ)/netstorm_bean.jar $(JAR_OBJ)/
	cp $(OBJ)/netstorm_bean.jar $(EXPORT_ROOT)/
	#cp ../jar/signed_jars/jmimemagic-0.1.0.jar $(EXPORT)/ #Required by FileMIMEType utlity class used by page dump
	#cp ../jar/signed_jars/jmimemagic-0.1.0.jar $(JAR_OBJ)/ #Required by FileMIMEType utlity class used by page dump
	#cp ../jar/signed_jars/apache-jakarta-oro.jar $(EXPORT)/ #Required by FileMIMEType utlity class used by page dump
	#cp ../jar/signed_jars/apache-jakarta-oro.jar $(JAR_OBJ)/ #Required by FileMIMEType utlity class used by page dump
        
FileBean.class : PathBean.java FileBean.java rptUtilsBean.java Config.java Log.java MD5Algo.java UserAdmin.java PDF.java Excel.java GenerateChart.java MergePage.java
	mkdir -p $(OBJ)/
	rm -f $(OBJ_PKG)/*.class $(OBJ)/netstorm_bean.jar
	${JAVAC} ${JAVA_FLAGS} -d $(OBJ) *.java VUserTrace/*.java nfLayouts/*.java runcommand/*.java GraphName/*.java Percentile/*.java Reporting/*.java
	cd $(OBJ); jar cf $(OBJ)/netstorm_bean.jar pac1
        


clean_export:
	rm -rf $(EXPORT)/netstorm_bean.jar

clean:
	rm -rf $(OBJ_PKG)/* $(OBJ)/*.jar
