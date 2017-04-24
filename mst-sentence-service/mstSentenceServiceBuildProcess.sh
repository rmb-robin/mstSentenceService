echo "mst tools build process"

cd "C:/Users/user1/Documents/MST-tools/MST-Tools"
mvn clean install
echo "mst tools build complete"
cd Target
cp MST-Tools-1.0.0.jar  "C:/Users/user1/Documents/mstSentenceService/mstSentenceService/mst-sentence-service/lib"
echo "installed mst tools jar in lib of sentence processing Service"
cd "C:/Users/user1/Documents/mstSentenceService/mstSentenceService/mst-sentence-service"
mvn clean install
echo "mst sentence service build complete"
