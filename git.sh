read -p "Commit note: " note
echo processing
echo $note
git add .
git commit -m "$note"
git push -u origin master
