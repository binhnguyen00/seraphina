SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
PROJECT_DIR="$(dirname "$SCRIPT_DIR")"
source $PROJECT_DIR/scripts/utils.sh

function show_help() {
  echo """
Usage: Application Management
 ./application.sh [COMMAND] [OPTION]

Start
 ./application.sh start:dev --jar=/path/to/application.jar
 ./application.sh start:prod --jar=/path/to/application.jar
 TODO: add run daemon

Build
 ./application.sh build

Stop
 ./application.sh stop

  """
}

function start() {
  local profile="dev"
  if [ "$1" = "start:prod" ]; then
    profile="prod"
  fi

  if has_opt "--jar" "$@"; then
    JAR_PATH=$(get_opt "--jar" "$@")
    if [ ! -f "$JAR_PATH" ]; then
      echo "Jar file not found: $JAR_PATH"
      exit 1
    fi

    java -jar "$JAR_PATH" \
      --spring.profiles.active="$profile"
  fi
}

function stop() {
  # implement this
}

function build() {
  if $windowsOS; then
    $PROJECT_DIR/gradlew.bat build
  else
    $PROJECT_DIR/gradlew build
  fi
}

COMMAND=$1;
if [ -n "$COMMAND" ]; then
  shift
else
  echo "No command provided. Showing help..."
  show_help
  exit 1
fi

if [[ "$COMMAND" == start:* ]] ; then
  start "$COMMAND" "$@"
elif [ "$COMMAND" = "stop" ] ; then
  stop
elif [ "$COMMAND" = "build" ] ; then
  build
fi