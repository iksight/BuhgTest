package source.views.admin.add;

import entity.Answers;
import entity.Questions;
import entity.Subjects;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import util.HibernateUtil;
import util.Messages;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by Kostya Nirchenko.
 *
 * @since 02.11.2016
 */
public class AddQuestionsController {

    public TextField thirdAnswerField;
    public TextField fourthAnswerField;
    public TextField secondAnswerField;
    public TextField firstAnswerField;
    public TextField questionField;
    public Button nextButton;
    public Button cancelButton;
    public RadioButton firstAnswerButton;
    public RadioButton secondAnswerButton;
    public RadioButton thirdAnswerButton;
    public RadioButton fourthAnswerButton;
    public ComboBox<Subjects> subjectField;
    private Stage stage;

    private ToggleGroup rightAnswerButton = new ToggleGroup();

    private boolean okClicked = false;

    private ObservableList<Subjects> subjectsObservableList = FXCollections.observableArrayList();

    public boolean isOkClicked() {
        return okClicked;
    }

    public void setStage(Stage stage) {
        this.stage = stage;
        firstAnswerButton.setToggleGroup(rightAnswerButton);
        secondAnswerButton.setToggleGroup(rightAnswerButton);
        thirdAnswerButton.setToggleGroup(rightAnswerButton);
        fourthAnswerButton.setToggleGroup(rightAnswerButton);
    }
    public void initialize() {
        if (subjectsObservableList.size() == 0) subjectsObservableList.clear();
        Session session = HibernateUtil.getSessionFactory().openSession();
        session.beginTransaction();
        try {
            Query query = session.createQuery("from Subjects");
            List<Subjects> subjectsList = (List<Subjects>) query.list();
            session.getTransaction().commit();
            session.close();
            subjectsObservableList.addAll(subjectsList.stream().collect(Collectors.toList()));
            subjectsList = null;
        } catch (Exception e) {
            Messages.showErrorMessage(e);
        }
        subjectField.setItems(subjectsObservableList);
    }

    public void nextButtonAction(ActionEvent actionEvent) {
        try {
            Session session = HibernateUtil.getSessionFactory().openSession();
            session.beginTransaction();
            Questions question = new Questions();
            if (allTyped()) {
                question.setQuestionText(questionField.getText());
                question.setSubjectId(subjectField.getSelectionModel().getSelectedItem().getId());
                session.save(question);
                int questionId = question.getId();
                session.getTransaction().commit();
                session.close();
                Session insertAnswer = HibernateUtil.getSessionFactory().openSession();
                insertAnswer.beginTransaction();
                Answers answer = new Answers(firstAnswerField.getText(),
                        secondAnswerField.getText(),
                        thirdAnswerField.getText(),
                        fourthAnswerField.getText(),
                        getRightAnswer((RadioButton) rightAnswerButton.getSelectedToggle()),
                        questionId);
                insertAnswer.save(answer);
                insertAnswer.getTransaction().commit();
                insertAnswer.close();
                okClicked = true;
                stage.close();
            } else Messages.showLoginErrorMessage("Заполните все поля");
        } catch (HibernateException e) {
            Messages.showErrorMessage(e);
        }
    }

    private String getRightAnswer(RadioButton id) {
        String rightAnswer = "";
        int _tmp = Integer.parseInt(id.getText());
        switch (_tmp) {
            case 1: rightAnswer = firstAnswerField.getText();
                break;
            case 2: rightAnswer =  secondAnswerField.getText();
                break;
            case 3: rightAnswer =  thirdAnswerField.getText();
                break;
            case 4: rightAnswer =  fourthAnswerField.getText();
                break;
        }
        return rightAnswer;
    }

    private boolean allTyped() {
        if (questionField.getText().trim().isEmpty() || firstAnswerField.getText().trim().isEmpty() ||
                secondAnswerField.getText().trim().isEmpty() || thirdAnswerField.getText().trim().isEmpty() ||
                fourthAnswerField.getText().trim().isEmpty() || rightAnswerButton.getSelectedToggle() == null ||
                subjectField.getSelectionModel().getSelectedItem() == null) {
            return false;
        } else return true;
    }

    public void cancelButtonAction(ActionEvent actionEvent) {
        stage.close();
    }
}
