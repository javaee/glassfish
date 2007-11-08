# sed script to label headers and footers for deletion.
# Note that processing starts after line 1, so that the first header is left untouched.
# Note that headers must be denoted as such, and so too must footers. This is
# so we can delete the appropriate amount of blank lines before and after each
# entity

# All patterns have been found by trial and error!

# Headers
2,$s/^User Commands.*$/HEADER DELETE/
2,$s/^Application Server Utility.*$/HEADER DELETE/
2,$s/^J2EE 1.4 SDK .*$/HEADER DELETE/
2,$s/^Java EE 5 .*$/HEADER DELETE/
2,$s/^SunOS 5.9 .*$/HEADER DELETE/
2,$s/^SunOS 5.10 .*$/HEADER DELETE/

# Footers
2,$s/^Sun ONE Application.*$/FOOTER DELETE/
2,$s/^Java 2 Platform.*$/FOOTER DELETE/
2,$s/^J2EE 1.4 SDK .*$/FOOTER DELETE/
2,$s/^J2EE SDK 1.4 .*$/FOOTER DELETE/
2,$s/^Java EE 5 .*$/FOOTER DELETE/
2,$s/^SunOS 5.9 .*$/FOOTER DELETE/
2,$s/^SunOS 5.10 .*$/HEADER DELETE/