function changeArticleSubmit(type) {
    let changeArticleForm = document.form_change_article
    let checks = document.getElementsByName("article_check")
    let article_id = null;

    // check selected elelent
    for (let index = 0; index < checks.length; index++) {
        if (checks[index].checked) {
            article_id = checks[index].getAttribute("data-id")
            break;
        }
    }
    if (article_id == null) {
        alert("記事を選択してください")
        return
    }

    switch (type) {
        case "Update":
            changeArticleForm.action = "/edit/" + article_id
            changeArticleForm.submit()
            break;
        default:
            break;
    }
}