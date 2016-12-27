all : 
	mvn clean install
	./export.sh compileExport
	
clean : 
	./export.sh clean
	