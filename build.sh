mvn clean install
tar -C client/target -xvzf client/target/tp2-client-1.0-SNAPSHOT-bin.tar.gz
tar -C server/target -xvzf server/target/tp2-server-1.0-SNAPSHOT-bin.tar.gz
chmod u+x client/target/tp2-client-1.0-SNAPSHOT/*
chmod u+x server/target/tp2-server-1.0-SNAPSHOT/*
cp -r client/target/tp2-client-1.0-SNAPSHOT/ scripts/client
chmod u+x scripts/**
mv scripts/client/query1.sh scripts/client/query1
mv scripts/client/query2.sh scripts/client/query2
mv scripts/client/query3.sh scripts/client/query3
mv scripts/client/query4.sh scripts/client/query4