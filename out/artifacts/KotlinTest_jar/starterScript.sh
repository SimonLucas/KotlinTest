#!/bin/sh
#$ -j y
#$ -pe smp 2
#$ -cwd
#$ -l h_rt=05:00:00
#$ -l h_vmem=30G
#$ -m bea
#$ -tc 35

module load java/1.8.0_152-oracle

java -Dagent="${1}" -DlutSize="$SGE_TASK_ID" -jar KotlinTest.jar > output.GoL_agent${1}_lutSize$SGE_TASK_ID.txt

