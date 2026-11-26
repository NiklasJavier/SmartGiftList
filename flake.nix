{
  description = "SmartGiftList development, container orchestration, and native E2E shell";

  inputs = {
    nixpkgs.url = "github:NixOS/nixpkgs/nixos-24.11";
  };

  outputs = { self, nixpkgs }:
    let
      systems = [
        "aarch64-darwin"
        "x86_64-darwin"
        "aarch64-linux"
        "x86_64-linux"
      ];
      forAllSystems = f: nixpkgs.lib.genAttrs systems (system: f nixpkgs.legacyPackages.${system});
    in
    {
      devShells = forAllSystems (pkgs:
        let
          jdk = pkgs.jdk21;
        in
        {
          default = pkgs.mkShell {
            packages = with pkgs; [
              jdk
              gradle
              just
              curl
              jq
              python3
              ruby
              shellcheck
              postgresql_16
              tomcat10
            ];

            JAVA_HOME = "${jdk}";
            JDK21_HOME = "${jdk}";
            CATALINA_HOME = "${pkgs.tomcat10}";
            ORG_GRADLE_PROJECT_org_gradle_java_installations_fromEnv = "JDK21_HOME";

            shellHook = ''
              export JAVA_HOME="${jdk}"
              export JDK21_HOME="${jdk}"
              export CATALINA_HOME="${pkgs.tomcat10}"
              echo "SmartGiftList devShell · JDK 21 + Gradle + Tomcat 10 + PostgreSQL 16 + just · run: just --list"
            '';
          };
        });
    };
}
