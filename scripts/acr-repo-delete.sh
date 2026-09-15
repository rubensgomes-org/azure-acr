#!/usr/bin/env bash
##
## acr-repo-delete.sh
##
## DESTRUCTIVE. Deletes an entire repository -- every tag and every
## manifest -- from an Azure Container Registry. There is no
## soft-delete and no recycle bin. Local counterpart of the
## acr-repo-delete GitHub Actions workflow.
##
## Requirements:
##  1) Bash functions libraries installed at "${HOME}/lib/sh-lib".
##  2) Azure CLI (az), already signed in with access to the
##     registry (run "az login" first).
##
## Author: Rubens Gomes
## NOTE:   Initial implementation was generated with AI assistance
##         and subsequently reviewed and approved by the author.
##
## This project's source code and documentation were generated
## with the assistance of Artificial Intelligence (AI). For more
## information, please refer to the `AI_DISCLAIMER.md` document
## located in the project's root directory.


#####################################################################
## GLOBAL CONSTANTS #################################################

# shell script program name
declare PRG
PRG="$(
  basename -- "${0}" || {
    printf "failed to determine the program basename." >&2
    exit 1
  }
)"

# minimum Bash major / minor version required
# shellcheck disable=SC2034
readonly BASH_MAJOR_VERSION="4"
# shellcheck disable=SC2034
readonly BASH_MINOR_VERSION="2"

# Set to TRUE when debugging code using bassupport-pro. This is
# required to bypass trap handlers which crashes code when running
# from debugger
readonly IS_DEBUGGER=FALSE

# temporary folder for this shell script
[[ -z "${TMP_DIR:-}" ]] && readonly TMP_DIR="/tmp/${PRG}"

# extra tools required by this script to run.
[[ ! -v REQUIRED_TOOLS ]] && readonly -a REQUIRED_TOOLS=(
  "az"
  "getopt"
)

#####################################################################
## INCLUDES #########################################################

[[ -d "${HOME}/lib/sh-lib" ]] || {
  printf "missing %s\n" "${HOME}/lib/sh-lib" >&2
  exit 1
}

# shellcheck source=/dev/null
source "${HOME}/lib/sh-lib/msg_lib.sh" || exit
# shellcheck source=/dev/null
source "${HOME}/lib/sh-lib/os_lib.sh" || exit
# shellcheck source=/dev/null
source "${HOME}/lib/sh-lib/sh_lib.sh" || exit
# shellcheck source=/dev/null
source "${HOME}/lib/sh-lib/misc_lib.sh" || exit


#####################################################################
## GLOBAL VARIABLES #################################################

# Image namespace. With artifactId this forms the repository
# <environment>/<artifactId>.
declare g_environment=

# Name of the EXISTING Azure Container Registry to delete from.
declare g_registry_name=

# Properties file holding artifactId. Defaults to
# "<git-root>/app/gradle.properties" when left unset.
declare g_properties_path=

# Must equal "DELETE REPO <registry> <environment>/<artifactId>"
# exactly.
declare g_confirm=

# Boolean flag used to run this shell script in "dry" run mode
# (inspects the target repository, deletes nothing)
declare g_is_dry_run=FALSE

# Boolean flag used to remove temporary files in trap handler
declare g_is_force_delete=FALSE


#####################################################################
## FUNCTIONS ########################################################

#####################################################################
## Prints help to stdout.
## Globals:
##  PRG
## Arguments:
##  none.
## Returns:
##   0 always
#####################################################################
help() {
  cat <<EOF

"${PRG}" permanently deletes a repository -- every tag and every
manifest -- from an Azure Container Registry.

Usage:
  ${PRG} -e <environment> -r <registry> -c <confirm> [options]

General Non Argument Options:

  -d, --debug                  prints debug messages
  -h, --help                   prints this help
  -q, --quiet                  prints only error|fatal messages
  -n, --dry-run                inspects only, does NOT delete
  -v, --verbose                adds extra details to messages
  -x, --trace                  traces commands

Argument Options:

  -e, --environment <env>      image namespace (default: lab)
  -r, --registry <name>        ACR name, not the login server
                                (default: crrgomesdev01)
  -p, --properties <path>      properties file holding artifactId
                                (default: app/gradle.properties)
  -c, --confirm <phrase>       must equal exactly:
                                "DELETE REPO <registry> <env>/<id>"

confirm (**required**):
  Safeguard against an accidental delete. Must match the resolved
  registry and repository exactly, e.g.
  "DELETE REPO crrgomesdev01 dev/azure-acr".

EOF
}

#####################################################################
## Prints usage to stderr.
## Globals:
##  PRG
## Arguments:
##  none.
## Exits:
##   2 always
#####################################################################
usage() {
  cat <<EOF
Usage:  ${PRG} [options]
More information with: "${PRG} -h"
EOF

  # return an error status
  return 2
} >&2

#####################################################################
## Initializes global variables to their initial state. That is,
## reset them.
## Globals:
##  g_confirm
##  g_environment
##  g_is_dry_run
##  g_is_force_delete
##  g_properties_path
##  g_registry_name
## Arguments:
##  None
## Returns:
##   0 if okay; something else if fails.
#####################################################################
reset_globals() {
  g_environment="lab"
  g_registry_name="crrgomesdev01"
  g_properties_path=
  g_confirm=
  g_is_dry_run=FALSE
  g_is_force_delete=FALSE
}

#####################################################################
## Parses user's command line input option arguments.
## Globals:
##  g_confirm
##  g_environment
##  g_is_dry_run
##  g_properties_path
##  g_registry_name
## Arguments:
##  Bash shell CLI input arguments.
## Returns:
##   0 if okay; something else if fails.
#####################################################################
parse_options() {

  # reset globals so that I can call this function multiple times
  # from within same unit test function stack frame, and not have
  # previous global values impact the next tests within the same
  # stack function frame.
  reset_globals

  local temp

  if ! temp=$(
    getopt \
      --o 'dhnqvxe:r:p:c:' \
      --long 'debug,dry-run,help,quiet,verbose,trace' \
      --long 'environment:,registry:,properties:,confirm:' \
      --name "${PRG}" \
      -- "${@}"
  ); then
    msg::error "failed to parse CLI input arguments."
    usage
    return 2
  fi

  eval set -- "${temp}"
  local quiet=FALSE

  while true; do

    case "${1}" in

      ########## --debug ############################################
      '-d' | '--debug')
        if [[ "${quiet}" == TRUE ]]; then
          msg::warn "-q option has been passed, and it overrides -d"
        else
          msg::enable_debug
        fi
        shift
        continue
        ;;

      ########## --help #############################################
      '-h' | '--help')
        help
        exit 0
        ;;

      ########## --dry-run ##########################################
      '-n' | '--dry-run')
        g_is_dry_run=TRUE
        shift
        continue
        ;;

      ########## --quiet ############################################
      '-q' | '--quiet')
        quiet=TRUE
        msg::enable_quiet
        shift
        continue
        ;;

      ########## --environment ######################################
      '-e' | '--environment')
        if [[ -z "${2:-}" || -z "${2//[[:space:]]/}" ]]; then
          msg::error "environment is missing or blank."
          usage
          return 2
        fi
        g_environment="$(sh::trim_space "${2}")" || return
        msg::debug "environment=%s\n" "${g_environment}"
        shift 2
        continue
        ;;

      ########## --registry #########################################
      '-r' | '--registry')
        if [[ -z "${2:-}" || -z "${2//[[:space:]]/}" ]]; then
          msg::error "registry is missing or blank."
          usage
          return 2
        fi
        g_registry_name="$(sh::trim_space "${2}")" || return
        msg::debug "registry_name=%s\n" "${g_registry_name}"
        shift 2
        continue
        ;;

      ########## --properties #######################################
      '-p' | '--properties')
        if [[ -z "${2:-}" || -z "${2//[[:space:]]/}" ]]; then
          msg::error "properties is missing or blank."
          usage
          return 2
        fi
        g_properties_path="$(sh::trim_space "${2}")" || return
        msg::debug "properties_path=%s\n" "${g_properties_path}"
        shift 2
        continue
        ;;

      ########## --confirm ##########################################
      '-c' | '--confirm')
        if [[ -z "${2:-}" ]]; then
          msg::error "confirm is missing."
          usage
          return 2
        fi
        g_confirm="${2}"
        shift 2
        continue
        ;;

      ########## --verbose ##########################################
      '-v' | '--verbose')
        msg::enable_verbose
        shift
        continue
        ;;

      ########## --trace ############################################
      '-x' | '--trace')
        msg::enable_tracing
        shift
        continue
        ;;

      ########## -- #################################################
      '--')
        shift
        break
        ;;

      ########## * ##################################################
      *)
        msg::arg_error "invalid option [%s].\n" "${1}"
        usage
        return 2
        ;;

    esac
  done

  # ensure mandatory CLI option "-c, --confirm" was provided by the
  # user.
  if [[ -z "${g_confirm:-}" ]]; then
    msg::error "missing [%s] CLI option argument.\n" \
      "--confirm <phrase>"
    return 2
  fi

  msg::debug "%s completed successfully.\n" "${FUNCNAME[0]}"
}

#####################################################################
## Checks required tool.
## Globals:
##  REQUIRED_TOOLS
## Arguments:
##  None
## Returns:
##   0 if okay, something else if fails.
#####################################################################
check_required_tool() {
  msg::debug "entering %s:%s\n" "${FUNCNAME[0]}" "${LINENO}"

  local tool
  for tool in "${REQUIRED_TOOLS[@]}"; do

    if ! os::is_installed "${tool}"; then
      msg::warn "Missing a required tool [%s].\n" "${tool}"
      return 127
    fi

  done

}

#####################################################################
## Resolves the repository coordinates from the properties file.
## Arguments:
##  1: properties file path
##  2: environment (image namespace)
## Outputs:
##  stdout: "<environment>/<artifactId>"
## Returns:
##   0 if okay; something else if fails.
#####################################################################
resolve_repository() {
  local -r properties_path="${1}"
  local -r environment="${2}"

  if [[ ! -f "${properties_path}" ]]; then
    msg::error "properties file not found: %s\n" \
      "${properties_path}"
    return 1
  fi

  local artifact_id
  artifact_id="$(
    misc::property_value "${properties_path}" "artifactId"
  )" || return

  if [[ -z "${artifact_id}" ]]; then
    msg::error "artifactId not found in %s\n" "${properties_path}"
    return 1
  fi

  printf '%s/%s\n' "${environment}" "${artifact_id}"
}

#####################################################################
## SAFEGUARD. Validates the typed confirmation phrase against the
## resolved registry and repository, so the phrase proves the caller
## knows what will actually be deleted.
## Arguments:
##  1: registry name
##  2: resolved repository, e.g. lab/azure-acr
##  3: typed confirmation phrase
## Returns:
##   0 if the phrase matches; something else if fails.
#####################################################################
validate_safeguard() {
  local -r registry_name="${1}"
  local -r repository="${2}"
  local -r typed_confirm="${3}"
  local -r expected="DELETE REPO ${registry_name} ${repository}"

  if [[ "${typed_confirm}" != "${expected}" ]]; then
    msg::error "confirmation phrase did not match exactly.\n"
    msg::error "expected: %s\n" "${expected}"
    msg::error "got:      %s\n" "${typed_confirm}"
    msg::error "nothing was deleted; Azure was not contacted.\n"
    return 1
  fi

  msg::info "safeguard passed. target: %s %s\n" \
    "${registry_name}" "${repository}"
}

#####################################################################
## Checks whether the Azure Container Registry exists and is visible
## to the signed-in principal. Deliberately uses "acr list" and not
## "acr show": "show" cannot distinguish "absent" from "the
## subscription could not be reached".
##
## An absent registry is not an error. A repository cannot outlive
## the registry that held it, so there is nothing left to delete.
## Arguments:
##  1: registry name
## Outputs:
##  stdout: the registry's login server, when it exists
## Returns:
##   0 if the registry exists; 1 if it does not; 2 on a
##   subscription-level failure.
#####################################################################
registry_exists() {
  local -r registry_name="${1}"
  local registries
  local login_server

  if ! registries="$(az acr list --query '[].name' -o tsv)"; then
    msg::error "could not list registries in this subscription.\n"
    return 2
  fi

  # -i: ACR names are case-insensitive in Azure, and "acr list"
  # returns the canonical casing.
  if ! printf '%s\n' "${registries}" \
    | grep -Fxqi "${registry_name}"; then
    return 1
  fi

  if ! login_server="$(
    az acr show --name "${registry_name}" \
      --query loginServer -o tsv
  )"; then
    msg::error "could not read the login server of [%s].\n" \
      "${registry_name}"
    return 2
  fi

  if [[ -z "${login_server}" ]]; then
    msg::error "registry [%s] returned an empty login server.\n" \
      "${registry_name}"
    return 2
  fi

  printf '%s\n' "${login_server}"
}

#####################################################################
## Checks whether the repository exists, and lists the tags that
## would be permanently deleted along with it. Deliberately uses
## "repository list" and not "show": "show" cannot distinguish
## "absent" from "the registry could not be reached".
## Arguments:
##  1: registry name
##  2: repository, e.g. lab/azure-acr
## Returns:
##   0 if the repository exists; 1 if it does not; 2 on a
##   registry-level failure.
#####################################################################
repository_exists() {
  local -r registry_name="${1}"
  local -r repository="${2}"
  local repositories

  if ! repositories="$(
    az acr repository list --name "${registry_name}" -o tsv
  )"; then
    msg::error "could not list repositories in [%s].\n" \
      "${registry_name}"
    return 2
  fi

  # -F -x: whole line, literal. A substring match would let
  # "lab/azure-acr" be satisfied by "lab/azure-acr-legacy".
  if ! printf '%s\n' "${repositories}" \
      | grep -Fxq "${repository}"; then
    return 1
  fi

  local tags
  tags="$(
    az acr repository show-tags --name "${registry_name}" \
      --repository "${repository}" -o tsv 2>/dev/null
  )" || true

  msg::info "repository [%s] exists; these tags will be deleted:\n" \
    "${repository}"

  if [[ -z "${tags}" ]]; then
    msg::info "  <untagged manifests only>\n"
  else
    local tag
    while IFS= read -r tag; do
      msg::info "  %s\n" "${tag}"
    done <<< "${tags}"
  fi
}

#####################################################################
## Permanently deletes a repository from an Azure Container
## Registry -- every tag and every manifest.
## Arguments:
##  1: registry name
##  2: repository, e.g. lab/azure-acr
## Returns:
##   0 if okay; something else if fails.
#####################################################################
delete_repository() {
  local -r registry_name="${1}"
  local -r repository="${2}"

  az acr repository delete \
    --name "${registry_name}" \
    --repository "${repository}" \
    --yes
}

#####################################################################
## Reads the catalog back to confirm the repository is really gone.
## A delete that reported success but left the repository behind
## must fail the run, not pass it.
## Arguments:
##  1: registry name
##  2: repository, e.g. lab/azure-acr
##  3: registry login server, for the report only
## Returns:
##   0 if confirmed gone; something else if fails.
#####################################################################
report_outcome() {
  local -r registry_name="${1}"
  local -r repository="${2}"
  local -r login_server="${3}"
  local repositories

  if ! repositories="$(
    az acr repository list --name "${registry_name}" -o tsv
  )"; then
    msg::error "deleted [%s], but could not read the catalog back to confirm it. Re-run to verify.\n" \
      "${repository}"
    return 1
  fi

  if printf '%s\n' "${repositories}" | grep -Fxq "${repository}"; then
    msg::error "repository [%s] is STILL in [%s]; delete did not take effect.\n" \
      "${repository}" "${registry_name}"
    return 1
  fi

  msg::info "deleted [%s] from [%s] (%s): gone, with all tags and manifests.\n" \
    "${repository}" "${registry_name}" "${login_server}"
}

#####################################################################
## Catches and handles signals defined in the "trap" command. Any
## cleanup necessary can go inside this function.
## Globals:
##  FUNCNAME
##  LINENO
##  TMP_DIR
##  FUNCNAME : Bash array for function names in the stack.
##  ERR  : trap any non-zero exit status
##  EXIT : trap sigspec for signal   (0) On exit from shell
##  HUP  : trap sigspec for SIGHUP   (1) Clean tidyup
##  INT  : trap sigspec for SIGINT   (2) Interrupt (CTRL-C)
##  QUIT : trap sigspec for SIGQUIT  (3) Quit
##  TERM : trap sigspec for SIGTERM (15) Terminate
##  g_is_force_delete
## Arguments:
##  None.
## Exits:
##   exit status code based on signal being handled.
#####################################################################
signal_handler() {
  local -r rc=$?
  msg::debug "entering %s:%s\n" "${FUNCNAME[0]}" "${LINENO}"

  local -r signal="${1:-}"
  msg::debug "handling signal [%s].\n" "${signal}"

  # disables following signals to avoid looping
  trap - ERR
  trap - EXIT
  trap - HUP  # signal 1
  trap - INT  # signal 2
  trap - QUIT # signal 3
  trap - TERM # signal 15

  local exit_code

  case "${signal}" in
    HUP)
      msg::warn \
        "the shell controlling terminal was hung: %s\n" \
       "SIGHUP"
      exit_code=129 # 1+128
      ;;
    INT)
      msg::warn \
        "execution interrupted by the user (control-c): %s\n" \
        "SIGINT"
      exit_code=130 # 2+128
      ;;
    QUIT)
      msg::warn \
        "execution interrupted due to unexpected event: %s\n" \
        "SIGQUIT"
      exit_code=131 # 3+128
      ;;
    TERM)
      msg::warn "someone asked the current execution to stop: %s\n" \
        "SIGTERM"
      exit_code=143 # 15+128
      ;;
    ERR)
      msg::warn "execution interrupted by Bash ERR signal: %s\n" \
        "SIGERR"
      exit_code=${rc}
      ;;
    EXIT)
      msg::debug "handling %s event\n" "SIGEXIT"
      ;;
    *)
      msg::warn "unexpected signal: %s\n" "${signal}"
      ;;
  esac

  msg::debug "attempting to delete tmp files at [%s].\n" "${TMP_DIR}"
  local folder
  folder="${TMP_DIR}"

  if [[ -d "${folder}" ]]; then
    local is_rm_local=FALSE

    # shellcheck disable=SC2154
    if [[ "${g_is_force_delete}" == TRUE ]]; then
      is_rm_local=TRUE
    else
      # logging or other display message should go to stderr because
      # the stdout is reserved to pass data between functions.
      printf "\nWARNING! This will remove folder:\n%s\n" \
        "${folder}" >&2
      msg::yes_no && is_rm_local=TRUE
    fi

    if [[ "${is_rm_local}" == TRUE ]]; then
      msg::debug "removing tmp folder [%s].\n" "${folder}"
      rm -fr "${folder}"
    fi

  fi

  if [[ -n "${exit_code:-}" ]]; then
    exit ${exit_code}
  fi

  exit
}

#####################################################################
## Main function.
## Globals:
##  IS_DEBUGGER
##  PRG
##  REQUIRED_TOOLS
##  TMP_DIR
##  g_confirm
##  g_environment
##  g_is_dry_run
##  g_is_force_delete
##  g_properties_path
##  g_registry_name
## Arguments:
##  Bash shell CLI input arguments.
## Returns:
##   0 if okay; something else if fails.
#####################################################################
main() {
  # sanity: start main function stack with a clean global state.
  reset_globals

  # --------------- >>> Check Required Tools <<< --------------------

  check_required_tool || return

  # --------------- >>> Parse CLI Input Arguments <<< ---------------

  parse_options "$@" || return

  msg::debug "Bash version: %s\n" "${BASH_VERSION}"
  msg::info "Running %s\n" "${PRG}"

  # --------------- >>>  Basic Shell Init / Trap Handlers <<< -------

  # bassupport-pro debugger crashes if following code is run
  if [[ "${IS_DEBUGGER}" != TRUE ]]; then
    sh::init || return
    local -ar signals=("ERR" "HUP" "INT" "TERM" "QUIT" "EXIT")
    sh::curry_trap_command "signal_handler" "${signals[*]}" || return
  fi

  # --------------- >>> Resolve Repository Coordinates <<< ----------

  local properties_path="${g_properties_path}"
  if [[ -z "${properties_path}" ]]; then
    local proj_root
    proj_root="$(misc::git_proj_root)" || return
    properties_path="${proj_root}/app/gradle.properties"
  fi

  local repository
  repository="$(
    resolve_repository "${properties_path}" "${g_environment}"
  )" || return
  msg::info "target repository: %s\n" "${repository}"

  # --------------- >>> SAFEGUARD <<< --------------------------------

  validate_safeguard "${g_registry_name}" "${repository}" \
    "${g_confirm}" || return

  # --------------- >>> Verify Azure Sign-In <<< ---------------------

  if ! az account show >/dev/null 2>&1; then
    msg::error "not signed in to Azure; run 'az login' first.\n"
    return 1
  fi

  # --------------- >>> Verify Registry And Repository <<< -----------

  local login_server
  local registry_rc=0
  login_server="$(
    registry_exists "${g_registry_name}"
  )" || registry_rc=$?

  if [[ "${registry_rc}" -eq 2 ]]; then
    return 1
  fi

  if [[ "${registry_rc}" -eq 1 ]]; then
    msg::info "registry [%s] does not exist. Nothing to delete.\n" \
      "${g_registry_name}"
    return 0
  fi

  local exists_rc=0
  repository_exists "${g_registry_name}" "${repository}" \
    || exists_rc=$?

  if [[ "${exists_rc}" -eq 2 ]]; then
    return 1
  fi

  if [[ "${exists_rc}" -eq 1 ]]; then
    msg::info "repository [%s] is not in [%s]. Nothing to delete.\n" \
      "${repository}" "${g_registry_name}"
    return 0
  fi

  # --------------- >>> Is this a Dry Run Only  <<< -----------------

  if [[ "${g_is_dry_run}" == TRUE ]]; then
    msg::info "dry run: skipping delete of [%s]\n" "${repository}"
    return 0
  fi

  # --------------- >>> Delete And Report <<< ------------------------

  delete_repository "${g_registry_name}" "${repository}" || return

  report_outcome "${g_registry_name}" "${repository}" \
    "${login_server}"
}

#####################################################################
## ------------------------------------------------------------------
## -------------------- >>> Main Program Body <<< -------------------
## ------------------------------------------------------------------

if ! main "$@"; then
  printf "\n%s failed!\n" "${PRG}" >&2
  exit 1
fi

printf "done\n"
