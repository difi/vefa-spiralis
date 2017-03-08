#!/usr/bin/env bash

# Skeleton script for starting, stopping and restarting spiralis-inbound.
#
# Modify to suit your needs.
#
# Executes the spiralis-inbound program, writing out logdata in accordance to the
# config file
#
export JAR="$HOME/distro/spiralis/spiralis-inbound.jar"
export NOHUP_OUT="$HOME/logs/spiralis-nohup.out"

export LOGBACK_CONF="$HOME/.spiralis/logback-spiralis-app.xml"

export PIDFILE="$HOME/.spiralis/spiralis.pid"   

export ARCHIVE_DIR="/var/peppol/ARCHIVE-IN" # Default archive directory
export INPUT_DIR="/var/peppol/IN"           # Default input directory
export GLOB="**.meta.json"                  # Default pattern for files to process

function usage {
        cat <<EOT

This program will scan the directory specified with the [-d|--directory] option for files matching
the patterh specified with [-g|--glob] option and move the files to the archive directory specified
with the [-a|--archive] option once they have been processed.

The program will run as a background process.

stdout and stderr is written to ${NOHUP_OUT}

The logback configuration file is read from ${LOGBACK_CONF}

    Usage:

    `basename $0` [--archive archive_dir] [--directory input_dir] [--glob glob_pattern]

    Default glob_pattern : ${GLOB}
    Default input dir    : ${INPUT_DIR}
    Default archive dir  : ${ARCHIVE_DIR}

EOT

    exit 4
}

function check_runtime_environment {
    if [ ! -r "$JAR" ]
    then
        echo "ERROR: could not read $JAR"
        exit 4
    fi

    if [ ! -r "$LOGBACK_CONF" ]
    then
        echo "ERROR: Logback config file $LOGBACK_CONF not found"
        exit 4
    fi
}

function stop {
    if [ -r "$PIDFILE" ]; then
        kill `cat "$PIDFILE"`
        rm "$PIDFILE"
    else
        echo "Spiralis-inbound not running"
    fi
}

function start {

    check_runtime_environment

    if [ -r ${PIDFILE} ]
    then
        echo "ERROR: $PIDFILE exists"
        echo "This indicates that another instance is running"
        echo "If this is not the case, remove $PIDFILE and try again"
        exit 4
    fi

    nohup java -Dlogback.configurationFile=$HOME/.spiralis/logback-spiralis-app.xml \
        -jar ${JAR}  \
        --archive /var/peppol/ARCHIVE --directory /var/peppol/IN --glob "**.meta.json" >$HOME/logs/spiralis-nohup.out 2>&1 &

    PID=$!
    RC=$?

    if [ ${RC} != 0 ]
    then
        echo "Execution failed"
    else
        echo ${PID} >${PIDFILE}
        echo "PID written to $PIDFILE"
    fi

    echo "Started"
    echo "See $NOHUP_OUT for output to stdout"
    echo "Review $LOGBACK_CONF for logging parameters"
}

function restart {
    stop
    start
}

#
# Parses the command line
while [[ $# -gt 0 ]]
do
    key="$1"

    case ${key} in
        -a|--archive)
            ARCHIVE_DIR="$2"
            if [ ! -r "$ARCHIVE_DIR" ]; then
                echo "ERROR: $ARCHIVE_DIR  does not exist"
                exit 4
            fi
            shift
        ;;
        -d|--directory)
            INPUT_DIR="$2"
            if [ ! -r "$INPUT_DIR" ]; then
                echo "ERROR: $INPUT_DIR does not exist"
                exit 4
            fi
            shift
        ;;
        start)
            echo "Starting ...."
            start
            shift
        ;;
        stop)
            echo "Stopping ...."
            stop
            shift
        ;;
        restart)
            echo "Restarting ....."
            restart
            shift
        ;;

        *)
            echo "ERROR Unknown option '$key'"
            usage
            shift
        ;;
    esac

    shift   # Past argument or value
done