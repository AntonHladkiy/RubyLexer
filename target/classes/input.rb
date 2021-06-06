class TasksController < ApplicationController
  def index
    @completed_sorted=params[:completed_sorted]
    @not_completed_sorted=params[:not_completed_sorted]
    @tasks_not_completed=current_user.tasks.where(["done = ? ", false])
    @tasks_completed=current_user.tasks.where(["done = ? ", true])
    if(@completed_sorted)
      @tasks_completed=current_user.tasks.where(["done = ? ", true]).order(:title)
    end
    if(@not_completed_sorted)
      @tasks_not_completed=current_user.tasks.where(["done = ? ", false]).order(:title)
    end
  end
  def new
    @task=Task.new
  end
  def batch_delete
    Task.where(id: params[:collection_ids]).destroy_all
    redirect_to tasks_path
  end
  def complete
    @task = Task.find(params[:id])
    @task.done=true
    @task.save
    redirect_to tasks_path
  end
  def uncomplete
    @task= Task.find(params[:id])
    @task.done=false
    @task.save
    redirect_to tasks_path
  end
  def show
    @task = Task.find(params[:id])
  end
  def edit
    @task = Task.find(params[:id])
  end
  def update
    @task = Task.find(params[:id])
    if(@task.update(task_params))
      redirect_to tasks_path
    else
      render 'edit'
    end
  end
  def destroy
    @task = Task.find(params[:id])
    @task.delete
    respond_to do |format|
      format.html { redirect_to tasks_path }
      format.json { head :no_content }
      format.js
    end
  end
  def create
    @task=current_user.tasks.create(task_params)
    @task.done=false
    if(@task.save)
      redirect_to tasks_path
    else
      render 'new'
    end
  end
  private def task_params
    params.require(:task).permit(:title,:description,:priority, :dueDate)
  end
end