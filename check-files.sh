md5 -r expected-results/* results/* | sort | awk '{print; if (FNR % 2 == 0) printf "\n";}'
