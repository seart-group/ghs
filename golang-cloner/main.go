package main

import (
	"flag"
	"fmt"
	"os"
	"os/exec"
	"path/filepath"
	"regexp"
	"time"

	"gopkg.in/yaml.v2"
)

type CloneStats struct {
	RepoURL       string        `yaml:"repository_url"`
	CloneFolder   string        `yaml:"clone_folder"`
	ExecutionTime time.Duration `yaml:"execution_time"`
}

func cloneRepo(repoURL *string, folderPath *string) CloneStats {
	startTime := time.Now()
	// Clone repository
	cmd := exec.Command("git", "clone", "--depth", "1", "--progress", "--verbose", *repoURL)
	cmd.Dir = *folderPath

	// Capture output in order to derive the cloned folder name
	stdout, err := cmd.StdoutPipe()
	cmd.Stderr = cmd.Stdout
	if err != nil {
		panic(fmt.Sprintf("Error acquiring stdout pipe: %s", err))
	}
	if err = cmd.Start(); err != nil {
		panic(fmt.Sprintf("Error starting `git` process: %s", err))
	}

	var cloneFolderName string
	for {
		tmp := make([]byte, 1024)
		_, stdout_err := stdout.Read(tmp)
		if *PRINT_STDOUT {
			fmt.Println(string(tmp))
		}
		if result := r.FindStringSubmatch(string(tmp)); result != nil {
			cloneFolderName = result[1]
			if !*PRINT_STDOUT {
				break
			}
		}
		if errors := git_err.FindStringSubmatch(string(tmp)); errors != nil {
			panic(fmt.Sprintf("git clone err: %s", errors[1]))
		}
		if stdout_err != nil {
			break
		}
	}

	cmd.Wait()
	executionTime := time.Since(startTime)

	// Get absolute path of clone folder
	cloneFolderPath := filepath.Join(*folderPath, cloneFolderName)
	cloneFolderPath, _ = filepath.Abs(cloneFolderPath)

	// Write statistics to stdout in YAML format
	stats := CloneStats{
		RepoURL:       *repoURL,
		CloneFolder:   cloneFolderPath,
		ExecutionTime: executionTime,
	}
	return stats
}

func cloneOrPullRepo(repoURL *string, folderPath *string) CloneStats {
	// TODO: Detect if folder already there, in that case pull
	return cloneRepo(repoURL, folderPath)
}

var PRINT_STDOUT *bool

// Parses the folder in which the repository is cloned
var r *regexp.Regexp

// Parses the 'fatal:' prefixed error messages from the git process
var git_err *regexp.Regexp

func main() {
	// TODO: Cloned repo folder size limit ? Automatic cleanup of old repos ?
	// Parse command line arguments
	repoURL := flag.String("url", "", "GitHub repository URL")
	folderPath := flag.String("folder", "cloned", "Folder to clone repository into")
	PRINT_STDOUT = flag.Bool("printstd", false, "Print the invoked commands output to stdout")
	flag.Parse()
	//

	if *repoURL == "" {
		panic("Please provide a GitHub repository URL")
	}

	r = regexp.MustCompile(`^Cloning into '(.*?)'`)
	git_err = regexp.MustCompile(`^fatal: (.+)`)

	// Create folder if it does not exist
	if _, err := os.Stat(*folderPath); os.IsNotExist(err) {
		os.Mkdir(*folderPath, 0755)
	}

	var stats CloneStats = cloneOrPullRepo(repoURL, folderPath)
	out, _ := yaml.Marshal(stats)
	fmt.Println(string(out))
}
